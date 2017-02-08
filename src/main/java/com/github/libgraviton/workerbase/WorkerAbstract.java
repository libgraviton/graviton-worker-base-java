/**
 * abstract base class for workers providing convenience
 */

package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import com.github.libgraviton.workerbase.model.GravitonRef;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workerbase.model.register.WorkerRegister;
import com.github.libgraviton.workerbase.model.register.WorkerRegisterSubscription;
import com.github.libgraviton.workerbase.model.status.EventStatus;
import com.github.libgraviton.workerbase.model.status.InformationType;
import com.github.libgraviton.workerbase.model.status.Status;
import com.github.libgraviton.workerbase.model.status.WorkerFeedback;
import com.github.libgraviton.workerbase.mq.MessageAcknowledger;
import com.github.libgraviton.workerbase.mq.QueueManager;
import com.github.libgraviton.workerbase.mq.exception.CannotAcknowledgeMessage;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
                    eventStatus.add(new WorkerFeedback(workerId, InformationType.ERROR, e.toString()));
                    update(eventStatus, workerId, Status.FAILED);
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
        statusHandler = new EventStatusHandler(properties.getProperty("graviton.eventStatusBaseUrl"));

        if (!shouldHandleRequest(queueEvent)) {
            // set status to ignored if the worker doesn't care about the event
            update(statusUrl, workerId, Status.IGNORED);
            return;
        }

        if (shouldAutoUpdateStatus()) {
            update(statusUrl, workerId, Status.WORKING);
        }

        // call the worker
        handleRequest(queueEvent);

        if (shouldAutoUpdateStatus()) {
            update(statusUrl, workerId, Status.DONE);
        }
    }

    protected void update(EventStatus eventStatus, String workerId, Status status) throws GravitonCommunicationException {
        if (eventStatus.shouldLinkAction(workerId)) {
            statusHandler.updateWithAction(eventStatus, workerId, status, getWorkerAction());
        } else {
            statusHandler.update(eventStatus, workerId, status);
        }
        if (status.isTerminatedState()) {
            reportToMessageQueue();
        }
    }

    protected void update(String eventStatusUrl, String workerId, Status status) throws GravitonCommunicationException {
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

        WorkerRegister workerRegister = new WorkerRegister();
        workerRegister.setId(workerId);
        workerRegister.setSubscription(getSubscriptions());

        HttpResponse<String> response;
        String registrationUrl = properties.getProperty("graviton.registerUrl");
        try {
            response = Unirest.put(registrationUrl)
                    .routeParam("workerId", workerId)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(JSON.std.asString(workerRegister))
                    .asString();
        } catch (UnirestException | IOException e) {
            throw new GravitonCommunicationException("Could not register worker '" + workerId + "' on backend at '" + registrationUrl + "'.", e);
        }

        LOG.debug("Worker register response code: " + response.getStatus());
        if (response.getStatus() == 204) {
            isRegistered = Boolean.TRUE;
        } else {
            throw new GravitonCommunicationException("Could not register worker '" + workerId + "' on backend at '" + registrationUrl + "'. Returned status: " + response.getStatus() + ", backend body: " + response.getBody());
        }
    }

    protected List<WorkerRegisterSubscription> getSubscriptions() {
        List<String> subscriptionKeys = Arrays.asList(properties.getProperty("graviton.subscription").split(","));
        List<WorkerRegisterSubscription> subscriptions = new ArrayList<>();

        for (String subscriptionKey: subscriptionKeys) {
            WorkerRegisterSubscription subscription = new WorkerRegisterSubscription();
            subscription.setEvent(subscriptionKey);
            subscriptions.add(subscription);
        }
        return subscriptions;
    }

    /**
     * Links to the action the worker is processing. The action itself contains a localized description.
     *
     * @return link to worker action
     */
    protected GravitonRef getWorkerAction() {
        String gravitonBaseUrl = properties.getProperty("graviton.base.url");
        String eventActionEndpoint = properties.getProperty("graviton.endpoint.event.action");
        String workerId = properties.getProperty("graviton.workerId");
        GravitonRef actionRef = new GravitonRef();
        actionRef.set$ref(gravitonBaseUrl + eventActionEndpoint + workerId + "-default");
        return actionRef;
    }

    public QueueManager getQueueManager() {
        return new QueueManager(properties);
    }
}
