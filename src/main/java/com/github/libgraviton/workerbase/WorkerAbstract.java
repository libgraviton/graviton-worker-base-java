/**
 * abstract base class for workers providing convenience
 */

package com.github.libgraviton.workerbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.model.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract WorkerAbstract class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public abstract class WorkerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerAbstract.class);

    /**
     * properties
     */
    protected Properties properties;
    
    /**
     * our worker id
     */
    protected String workerId;

    /**
     * our worker status
     */
    protected WorkerStatus status;

    /**
     * is worker registered
     */
    protected Boolean isRegistered = Boolean.FALSE;

    /**
     * was last status update successfully sent to backend
     */
    protected Boolean lastStatusUpdateSuccessful = Boolean.TRUE;


    /**
     * <p>Getter for the field <code>properties</code>.</p>
     *
     * @return a {@link java.util.Properties} object.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * <p>Getter for the field <code>workerId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWorkerId() {
        return workerId;
    }

    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public WorkerStatus getStatus() {
        return status;
    }

    /**
     * <p>getRegistered.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getRegistered() {
        return isRegistered;
    }

    /**
     * <p>Getter for the field <code>lastStatusUpdateSuccessful</code>.</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getLastStatusUpdateSuccessful() {
        return lastStatusUpdateSuccessful;
    }
        
    /**
     * worker logic is implemented here
     *
     * @param body message body as object
     * @throws WorkerException if any.
     * @throws java.lang.Exception if any.
     */
    abstract public void handleRequest(QueueEvent body) throws Exception;
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     *
     * @param body queueevent object
     * @return boolean true if not, false if yes
     */
    abstract public boolean shouldHandleRequest(QueueEvent body);
    
    /**
     * initializes this worker, will be called by the library
     *
     * @param properties properties
     * @throws java.lang.Exception if any.
     */
    public final void initialize(Properties properties) throws Exception {
        this.properties = properties;
        workerId = properties.getProperty("graviton.workerId");

        if (shouldAutoRegister()) {
            registerWorker();
        }
    }

    /**
     * outer function that will be called on an queue event
     *
     * @param consumerTag consumer tag (aka routing key)
     * @param queueEvent queue event
     * @throws java.io.IOException if any.
     */
    public final void handleDelivery(String consumerTag, QueueEvent queueEvent) throws IOException {

        String statusUrl = queueEvent.getStatus().get$ref();
        
        if (!shouldHandleRequest(queueEvent)) {
            return;
        }
        
        if (shouldAutoUpdateStatus()) {
            status = WorkerStatus.WORKING;
            updateStatusAtUrl(statusUrl);
        }

        try {
            // call the worker
            handleRequest(queueEvent);
            
            if (shouldAutoUpdateStatus()) {
                status = WorkerStatus.DONE;
                updateStatusAtUrl(statusUrl);
            }
            
        } catch (Exception e) {
            LOG.error("Error in worker: " + workerId, e);

            if (shouldAutoUpdateStatus()) {
                status = WorkerStatus.FAILED;
                updateStatusAtUrl(statusUrl, e.toString());
            }
            
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
     * will be called after we're initialized, can contain some initial logic in the worker
     */
    public void onStartUp() throws WorkerException
    {
    }

    /**
     * convenience function to set the status
     *
     * @param statusUrl status url
     * @param status status which status
     * @deprecated replaced by {@link #updateStatusAtUrl(String statusUrl)} after setting status variable.
     */
    protected void setStatus(String statusUrl, WorkerStatus status) {
        this.status = status;
        this.updateStatusAtUrl(statusUrl);
    }

    /**
     * Update status with a string based error information
     *
     * @param statusUrl status url
     * @param status status which status
     * @param errorInformation error information message
     * @deprecated replaced by {@link #updateStatusAtUrl(String statusUrl, String errorInformation)} after setting status variable.
     */
    protected void setStatus(String statusUrl, WorkerStatus status, String errorInformation) {
        this.status = status;
        this.updateStatusAtUrl(statusUrl, errorInformation);
    }

    /**
     * update the status to our backend
     *
     * @param statusUrl status url
     * @param status status which status
     * @param informationEntry an EventStatusInformation instance that will be added to the information array
     * @deprecated replaced by {@link #updateStatusAtUrl(String statusUrl, EventStatusInformation informationEntry)} after setting status variable.
     */
    protected void setStatus(String statusUrl, WorkerStatus status, EventStatusInformation informationEntry) {
        this.status = status;
        this.updateStatusAtUrl(statusUrl, informationEntry);
    }

    /**
     * convenience function to update the status
     *
     * @param statusUrl status url
     */
    protected void updateStatusAtUrl(String statusUrl) {
        this.updateStatusAtUrl(statusUrl, "");
    }

    /**
     * Update status with a string based error information
     *
     * @param statusUrl url to status document
     * @param errorInformation error information message
     */
    protected void updateStatusAtUrl(String statusUrl, String errorInformation) {
        
        EventStatusInformation statusInformation = null;
        
        if (errorInformation.length() > 0) {
            statusInformation = new EventStatusInformation();
            statusInformation.setWorkerId(this.workerId);
            statusInformation.setType(WorkerInformationType.ERROR);
            statusInformation.setContent(errorInformation);
        }
        
        this.updateStatusAtUrl(statusUrl, statusInformation);
    }
    
    /**
     * update the status to our backend
     *
     * @param statusUrl url to status document
     * @param informationEntry an EventStatusInformation instance that will be added to the information array
     */
    protected void updateStatusAtUrl(String statusUrl, EventStatusInformation informationEntry) {
        try {
            EventStatus eventStatus = getEventStatus(statusUrl);
            modifyStatusForWorker(eventStatus);
            addStatusInformation(informationEntry, eventStatus);
            updateStatus(statusUrl, eventStatus);
        } catch (WorkerException e) {
            LOG.error("[F] Backend error on status update!", e);
        } catch (Exception e) {
            LOG.error("[F] Exception on status update!", e);
        }
    }

    /**
     * registers our worker with the backend
     *
     * @throws java.lang.Exception if any.
     */
    protected void registerWorker() throws Exception {

        WorkerRegister workerRegister = new WorkerRegister();
        workerRegister.setId(this.workerId);
        
        List<String> subscriptionKeys = Arrays.asList(this.properties.getProperty("graviton.subscription").split(","));
        List<WorkerRegisterSubscription> subscriptions = new ArrayList<>();
        
        for (String subscriptionKey: subscriptionKeys) {
            WorkerRegisterSubscription subscription = new WorkerRegisterSubscription();
            subscription.setEvent(subscriptionKey);
            subscriptions.add(subscription);
        }
        
        workerRegister.setSubscription(subscriptions);
        
        HttpResponse<String> response = 
                Unirest.put(this.properties.getProperty("graviton.registerUrl"))
                .routeParam("workerId", this.workerId)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(JSON.std.asString(workerRegister))
                .asString();

        LOG.info("[*] Worker register response code: " + response.getStatus());

        if (response.getStatus() == 204) {
            isRegistered = Boolean.TRUE;
        } else {
            throw new WorkerException("Could not register worker on backend! Returned status: " + response.getStatus() + ", backend body: " + response.getBody());
        }        
    }

    private void updateStatus(String statusUrl, EventStatus eventStatus) throws UnirestException, IOException, WorkerException {
        // send the new status to the backend
        HttpResponse<String> response = Unirest.put(statusUrl).header("Content-Type", "application/json").body(JSON.std.asString(eventStatus)).asString();

        LOG.info("[x] Updated status to '" + status + "'");

        if (response.getStatus() == 204) {
            lastStatusUpdateSuccessful = Boolean.TRUE;
            LOG.info("[x] Updated status to '" + status + "' on '" + statusUrl + "'");
        } else {
            lastStatusUpdateSuccessful = Boolean.FALSE;
            throw new WorkerException("Could not update status on backend! Returned status: " + response.getStatus() + ", backend body: " + response.getBody());
        }
    }

    private void addStatusInformation(EventStatusInformation informationEntry, EventStatus eventStatus) {
        // add information entry if present
        if (informationEntry != null) {
            // ensure list presence
            if (eventStatus.getInformation() == null) {
                eventStatus.setInformation(new ArrayList<EventStatusInformation>());
            }
            eventStatus.getInformation().add(informationEntry);
        }
    }

    private void modifyStatusForWorker(EventStatus eventStatus) {
        // modify our status in the status array
        List<EventStatusStatus> wokerStates = eventStatus.getStatus();
        for (EventStatusStatus wokerState : wokerStates) {
            if (wokerState.getWorkerId().equals(this.workerId)) {
                wokerState.setStatus(status);
            }
        }
    }

    private EventStatus getEventStatus(String statusUrl) throws UnirestException, IOException {
        HttpResponse<String> response = Unirest.get(statusUrl).header("Accept", "application/json").asString();
        return JSON.std.beanFrom(EventStatus.class, response.getBody());
    }
    
}
