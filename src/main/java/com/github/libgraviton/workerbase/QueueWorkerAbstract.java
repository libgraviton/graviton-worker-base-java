package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.GravitonAuthApi;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventstatusaction.document.EventStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorker;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorkerSubscription;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.exception.CannotAcknowledgeMessage;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.google.common.util.concurrent.AtomicLongMap;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import okhttp3.HttpUrl;

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
public abstract class QueueWorkerAbstract extends BaseWorker implements QueueWorkerInterface {

    private final AtomicLongMap<String> eventStates = AtomicLongMap.create();

    protected EventStatusHandler statusHandler;

    protected boolean useTransientHeaders = true;

    protected String messageId;

    protected MessageAcknowledger acknowledger;

    protected GravitonAuthApi gravitonApi;
    
    /**
     * initializes this worker, will be called by the library
     *
     * @param properties properties
     * @throws WorkerException when a problem occurs that prevents the Worker from working properly
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     *
     */
    @Override
    public final void initialize(Properties properties) throws WorkerException, GravitonCommunicationException  {
        super.initialize(properties);

        gravitonApi = initGravitonApi();
        statusHandler = new EventStatusHandler(gravitonApi);

        // create our metrics..
        for (String theState : List.of(QueueEvent.STATE_RECEIVED, QueueEvent.STATE_IGNORED, QueueEvent.STATE_HANDLED, QueueEvent.STATE_ERRORED)) {
            eventStates.put(theState, 0);

            Tags tags = Tags.of("state", theState);
            Gauge.builder("worker_queue_events", eventStates, map -> map.get(theState))
                    .tags(tags)
                    .description("Total worker queue events received/handled/ignored/errors.")
                    .register(Metrics.globalRegistry);
        }

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

        // add as received
        eventStates.incrementAndGet(QueueEvent.STATE_RECEIVED);

        this.messageId = messageId;
        this.acknowledger = acknowledger;

        String statusUrl = convertToGravitonUrl(queueEvent.getStatus().get$ref());
        try {
            // any transient headers?
            if (shouldUseTransientHeaders() && isUseTransientHeaders()) {
                gravitonApi.setTransientHeaders(queueEvent.getTransientHeaders());
            }

            if (!shouldHandleRequest(queueEvent)) {
                eventStates.incrementAndGet(QueueEvent.STATE_IGNORED);

                if (shouldAutoUpdateStatus()) {
                    update(statusUrl, workerId, EventStatusStatus.Status.IGNORED);
                }

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

            // mark as handled
            eventStates.incrementAndGet(QueueEvent.STATE_HANDLED);
        } catch (Throwable e) {

            // mark as errored
            eventStates.incrementAndGet(QueueEvent.STATE_ERRORED);

            LOG.error("Error in worker: " + workerId, e);

            if (shouldAutoUpdateStatus()) {
                try {
                    statusHandler.updateToErrorState(statusUrl, workerId, e.toString());
                } catch (GravitonCommunicationException e1) {
                    // don't log again in case if previous exception was already a GravitonCommunicationException.
                    if (!(e instanceof GravitonCommunicationException)) {
                        LOG.error("Unable to update worker status at '" + statusUrl + "'.");
                    }
                }
            }

            // acknowledge message here as we are done processing
            if (shouldAutoAcknowledgeOnException()) {
                reportToMessageQueue();
            }
        } finally {
            gravitonApi.clearTransientHeaders();
        }
    }

    /**
     * Rewrite the status URL in relation to the configured GravitonApi Base url.
     *
     * @param url url
     * @return corrected url
     */
    private String convertToGravitonUrl(String url) {
        HttpUrl baseUrl = HttpUrl.parse(gravitonApi.getBaseUrl());

        // convert
        HttpUrl targetUrl = HttpUrl
            .parse(url)
            .newBuilder()
            .host(baseUrl.host())
            .port(baseUrl.port())
            .scheme(baseUrl.scheme())
            .build();

        return targetUrl.toString();
    }

    @Deprecated
    final protected void processDelivery(QueueEvent queueEvent, String statusUrl) throws WorkerException, GravitonCommunicationException {
        // DO NOT IMPLEMENT processDelivery anymore in the worker! marking as final, it's not called anymore..
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
     * can be overriden by worker implementation. should the lib acknowledge the queue action when an exception happened?
     *
     * @return true if yes, false if not
     */
    public Boolean shouldAutoAcknowledgeOnException()
    {
        return false;
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
     * the worker needs to OPT OUT here - only if this is true, the "transient headers" feature is used
     * where the backend headers are 1:1 forwarded (= transient) to subsequent requests inside the delivery
     * handling.
     *
     * this is mostly needed for workers that need to be aware in the tenant and username in the context of the
     * request handling - as such, they basically impersonate the same user to the backend as the actual user that
     * sent the Queue event
     *
     * @return
     */
    public Boolean shouldUseTransientHeaders() {
        return true;
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
        } catch (CommunicationException e) {
            throw new GravitonCommunicationException("Unable to register worker '" + workerId + "'.", e);
        }
    }

    protected List<EventWorkerSubscription> getSubscriptions() {
        String[] subscriptionKeys = properties.getProperty("graviton.subscription").split(",");
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

    protected GravitonAuthApi initGravitonApi() {
        return new GravitonAuthApi(properties);
    }

    public void setUseTransientHeaders(boolean useTransientHeaders) {
        this.useTransientHeaders = useTransientHeaders;
    }

    public boolean isUseTransientHeaders() {
        return useTransientHeaders;
    }

    /**
     * detects if an object is run from inside of a jar file.
     *
     * @param obj object to test
     * @return true if worker is run from a jar file else false
     */
    @Deprecated
    public static boolean isWorkerStartedFromJARFile(Object obj) {
        return WorkerUtil.isJarContext(obj);
    }
}
