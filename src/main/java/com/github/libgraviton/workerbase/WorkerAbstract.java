package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusInformation;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventstatusaction.document.EventStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorker;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorkerSubscription;
import com.github.libgraviton.messaging.MessageAcknowledger;
import com.github.libgraviton.messaging.exception.CannotAcknowledgeMessage;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import com.github.libgraviton.workerbase.model.QueueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * <p>Abstract WorkerAbstract class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public abstract class WorkerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerAbstract.class);

    protected Properties properties;

    protected String workerId;

    protected EventStatusHandler statusHandler;

    protected Boolean isRegistered = Boolean.FALSE;

    protected String messageId;

    protected MessageAcknowledger acknowledger;

    protected GravitonApi gravitonApi;

    public Properties getProperties() {
        return properties;
    }

    public String getWorkerId() {
        return workerId;
    }

    public Boolean getRegistered() {
        return isRegistered;
    }


    /**
     * detects if an object is run from inside of a jar file.
     *
     * @param obj object to test
     * @return true if worker is run from a jar file else false
     */
    public static boolean isWorkerStartedFromJARFile(Object obj) {
        String protocol = obj.getClass().getResource("").getProtocol();

        return protocol.equals("jar");
    }

    /**
     * worker logic is implemented here
     *
     * @param body message body as object
     * @throws WorkerException whenever a worker is unable to finish its task.
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     */
    abstract public void handleRequest(QueueEvent body) throws WorkerException, GravitonCommunicationException;
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     *
     * @param body queueevent object
     * @return boolean true if not, false if yes
     * @throws WorkerException whenever a worker is unable to determine if it should handle the request
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     */
    abstract public boolean shouldHandleRequest(QueueEvent body) throws WorkerException, GravitonCommunicationException;
    
    /**
     * initializes this worker, will be called by the library
     *
     * @param properties properties
     * @throws WorkerException when a problem occurs that prevents the Worker from working properly
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     *
     */
    public final void initialize(Properties properties) throws WorkerException, GravitonCommunicationException  {
        this.properties = properties;

        try {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    LOG.error("Uncaught Exception", e);
                }
            });
        } catch (SecurityException e) {
            LOG.error("Could not set the Default Uncaught Exception Handler", e);
        }

        this.gravitonApi = initGravitonApi();
        workerId = properties.getProperty("graviton.workerId");

        if (shouldAutoRegister()) {
            register();
        }
    }

    /**
     * outer function that will be called on an queue event
     *
     * @param messageId delivery tag from envelope
     * @param acknowledger registered acknowledger
     * @param queueEvent queue event
     */
    public final void handleDelivery(QueueEvent queueEvent, String messageId, MessageAcknowledger acknowledger) {

        this.messageId = messageId;
        this.acknowledger = acknowledger;

        String statusUrl = queueEvent.getStatus().get$ref();
        try {
            processDelivery(queueEvent, statusUrl);
        } catch (Exception e) {
            LOG.error("Error in worker: " + workerId, e);

            if (shouldAutoUpdateStatus()) {
                try {
                    EventStatus eventStatus = statusHandler.getEventStatusFromUrl(statusUrl);
                    EventStatusInformation information = new EventStatusInformation();
                    information.setWorkerId(workerId);
                    information.setType(EventStatusInformation.Type.ERROR);
                    information.setContent(e.toString());
                    eventStatus.getInformation().add(information);
                    update(eventStatus, workerId, EventStatusStatus.Status.FAILED);
                } catch (GravitonCommunicationException e1) {
                    // don't log again in case if previous exception was already a GravitonCommunicationException.
                    if (!(e instanceof GravitonCommunicationException)) {
                        LOG.error("Unable to update worker status at '" + statusUrl + "'.");
                    }
                    reportToMessageQueue();
                }
            }
        }
    }

    protected void processDelivery(QueueEvent queueEvent, String statusUrl) throws WorkerException, GravitonCommunicationException {
        statusHandler = new EventStatusHandler(gravitonApi);

        if (!shouldHandleRequest(queueEvent)) {
            // set status to ignored if the worker doesn't care about the event
            update(statusUrl, workerId, EventStatusStatus.Status.IGNORED);
            return;
        }

        if (shouldAutoUpdateStatus()) {
            update(statusUrl, workerId, EventStatusStatus.Status.WORKING);
        }

        // call the worker
        handleRequest(queueEvent);

        if (shouldAutoUpdateStatus()) {
            update(statusUrl, workerId, EventStatusStatus.Status.DONE);
        }
    }

    protected void update(EventStatus eventStatus, String workerId, EventStatusStatus.Status status) throws GravitonCommunicationException {
        if (shouldLinkAction(workerId, eventStatus.getStatus())) {
            statusHandler.updateWithAction(eventStatus, workerId, status, getWorkerAction());
        } else {
            statusHandler.update(eventStatus, workerId, status);
        }
        if (isTerminatedState(status)) {
            reportToMessageQueue();
        }
    }

    protected boolean isTerminatedState(EventStatusStatus.Status status) {
        return Arrays.asList(
                EventStatusStatus.Status.DONE,
                EventStatusStatus.Status.FAILED,
                EventStatusStatus.Status.IGNORED)
                .contains(status);
    }

    protected boolean shouldLinkAction(String workerId, List<EventStatusStatus> status) {
        for (EventStatusStatus statusEntry : status) {
            if (workerId.equals(statusEntry.getWorkerId())
                    && statusEntry.getAction() != null) {
                return false;
            }
        }
        return true;
    }

    protected void update(String eventStatusUrl, String workerId, EventStatusStatus.Status status) throws GravitonCommunicationException {
        update(statusHandler.getEventStatusFromUrl(eventStatusUrl), workerId, status);
    }

    private void reportToMessageQueue() {
        try {
            acknowledger.acknowledge(messageId);
        } catch (CannotAcknowledgeMessage e) {
            LOG.error("Unable to ack messageId '" + messageId + "' on message queue.");
        }
    }

    /**
     * can be overriden by worker implementation. should the lib automatically update the EventStatus in the backend?
     *
     * @return true if yes, false if not
     */
    public Boolean shouldAutoUpdateStatus()
    {
        return true;
    }

    /**
     * can be overriden by worker implementation. should the lib automatically register the worker?
     *
     * @return true if yes, false if not
     */
    public Boolean shouldAutoRegister()
    {
        return true;
    }

    /**
     * will be called after we're initialized, can contain some initial logic in the worker.
     *
     * @throws WorkerException when a problem occurs that prevents the Worker from working properly
     */
    public void onStartUp() throws WorkerException
    {
    }

    /**
     * registers our worker with the backend
     *
     * @throws GravitonCommunicationException when registering worker is not possible
     */
    protected void register() throws GravitonCommunicationException {
        EventWorker eventWorker = new EventWorker();
        eventWorker.setId(workerId);
        eventWorker.setSubscription(getSubscriptions());

        try {
            gravitonApi.put(eventWorker).execute();
            isRegistered = Boolean.TRUE;
        } catch (CommunicationException e) {
            throw new GravitonCommunicationException("Unable to register worker '" + workerId + "'.", e);
        }
    }

    protected List<EventWorkerSubscription> getSubscriptions() {
        List<String> subscriptionKeys = Arrays.asList(properties.getProperty("graviton.subscription").split(","));
        List<EventWorkerSubscription> subscriptions = new ArrayList<>();
        for (String subscriptionKey: subscriptionKeys) {
            EventWorkerSubscription subscription = new EventWorkerSubscription();
            if (!subscriptionKey.isEmpty()) {
                subscription.setEvent(subscriptionKey.trim());
                subscriptions.add(subscription);
            }
        }
        return subscriptions;
    }

    /**
     * Links to the action the worker is processing. The action itself contains a localized description.
     *
     * @return link to worker action
     */
    protected EventStatusStatusAction getWorkerAction() {
        String eventStatusActionEndpointUrl = gravitonApi
                .getEndpointManager()
                .getEndpoint(EventStatusAction.class.getName())
                .getUrl();

        String workerId = properties.getProperty("graviton.workerId");
        EventStatusStatusAction action = new EventStatusStatusAction();
        action.set$ref(eventStatusActionEndpointUrl + workerId + "-default");
        return action;
    }

    public QueueManager getQueueManager() {
        return new QueueManager(properties);
    }

    protected GravitonApi initGravitonApi() {
        return new GravitonAuthApi(properties);
    }
}
