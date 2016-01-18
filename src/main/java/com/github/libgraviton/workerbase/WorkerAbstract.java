/**
 * abstract base class for workers providing convenience
 */

package com.github.libgraviton.workerbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.model.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
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
     * status constants
     */
    public static final String STATUS_WORKING = "working";
    /** Constant <code>STATUS_DONE="done"</code> */
    public static final String STATUS_DONE = "done";
    /** Constant <code>STATUS_FAILED="failed"</code> */
    public static final String STATUS_FAILED = "failed";

    /** Constant <code>INFORMATION_TYPE_DEBUG="debug"</code> */
    public static final String INFORMATION_TYPE_DEBUG = "debug";
    /** Constant <code>INFORMATION_TYPE_INFO="info"</code> */
    public static final String INFORMATION_TYPE_INFO = "info";
    /** Constant <code>INFORMATION_TYPE_WARNING="warning"</code> */
    public static final String INFORMATION_TYPE_WARNING = "warning";
    /** Constant <code>INFORMATION_TYPE_ERROR="error"</code> */
    public static final String INFORMATION_TYPE_ERROR = "error";

    /**
     * properties
     */
    protected Properties properties;
    
    /**
     * our worker id
     */
    protected String workerId;

    /**
     * our worker state
     */
    protected String state;

    /**
     * is worker registered
     */
    protected Boolean isRegistered = Boolean.FALSE;

    /**
     * was last status update successfully sent to backend
     */
    protected Boolean lastStatusUpdateSuccessful = Boolean.TRUE;


    public Properties getProperties() {
        return properties;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getState() {
        return state;
    }

    public Boolean getRegistered() {
        return isRegistered;
    }

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
    abstract public boolean isConcerningRequest(QueueEvent body);
    
    /**
     * initializes this worker, will be called by the library
     *
     * @param properties properties
     * @throws java.lang.Exception if any.
     */
    public final void initialize(Properties properties) throws Exception {
        this.properties = properties;
        this.workerId = this.properties.getProperty("graviton.workerId");

        if (this.doAutoRegister()) this.registerWorker();
    }

    /**
     * outer function that will be called on an queue event
     *
     * @param consumerTag consumer tag (aka routing key)
     * @param qevent queue event
     * @throws java.io.IOException if any.
     */
    public final void handleDelivery(String consumerTag, QueueEvent qevent)
            throws IOException {

        String statusUrl = qevent.getStatus().get$ref();
        
        if (this.isConcerningRequest(qevent) == false) {
            return;
        }
        
        if (this.doAutoUpdateStatus()) {
            state = STATUS_WORKING;
            this.updateStatusAtUrl(statusUrl);
        }

        try {
            // call the worker
            this.handleRequest(qevent);
            
            if (this.doAutoUpdateStatus()) {
                state = STATUS_DONE;
                this.updateStatusAtUrl(statusUrl);
            }
            
        } catch (Exception e) {
            LOG.error("Error in worker: " + workerId, e);

            if (this.doAutoUpdateStatus()) {
                state = STATUS_FAILED;
                this.updateStatusAtUrl(statusUrl, e.toString());
            }
            
        }
    }    
    
    /**
     * can be overriden by worker implementation. should the lib automatically update the EventStatus in the backend?
     *
     * @return true if yes, false if not
     */
    public Boolean doAutoUpdateStatus()
    {
        return true;
    }

    /**
     * can be overriden by worker implementation. should the lib automatically register the worker?
     *
     * @return true if yes, false if not
     */
    public Boolean doAutoRegister()
    {
        return true;
    }
    
    /**
     * will be called after we're initialized, can contain some initial logic in the worker
     */
    public void onStartUp()
    {
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
        
        EventStatusInformation infoObj = null;
        
        if (errorInformation.length() > 0) {
            infoObj = new EventStatusInformation();
            infoObj.setWorkerId(this.workerId);
            infoObj.setType(INFORMATION_TYPE_ERROR);
            infoObj.setContent(errorInformation);
        }
        
        this.updateStatusAtUrl(statusUrl, infoObj);
    }
    
    /**
     * update the status to our backend
     *
     * @param statusUrl url to status document
     * @param informationEntry an EventStatusInformation instance that will be added to the information array
     */
    protected void updateStatusAtUrl(String statusUrl, EventStatusInformation informationEntry) {
        try {
            HttpResponse<String> response = Unirest.get(statusUrl).header("Accept", "application/json").asString();

            EventStatus eventStatus = JSON.std.beanFrom(EventStatus.class, response.getBody());

            // modify our status in the status array
            ArrayList<EventStatusStatus> statusObj = eventStatus.getStatus();
            for (int i = 0; i < statusObj.size(); i++) {
                EventStatusStatus statusEntry = statusObj.get(i);
                if (statusEntry.getWorkerId().equals(this.workerId)) {
                    statusEntry.setStatus(state);
                    statusObj.set(i, statusEntry);
                }
            }

            eventStatus.setStatus(statusObj);

            // add information entry if present
            if (informationEntry instanceof EventStatusInformation) {
                // ensure list presence
                if (!(eventStatus.getInformation() instanceof ArrayList<?>)) {
                    eventStatus.setInformation(new ArrayList<EventStatusInformation>());
                }
                eventStatus.getInformation().add(informationEntry);
            }

            // send the new status to the backend
            HttpResponse<String> putResp = Unirest.put(statusUrl).header("Content-Type", "application/json").body(JSON.std.asString(eventStatus)).asString();

            LOG.info("[x] Updated status to '" + state + "'");

            if (putResp.getStatus() == 204) {
                lastStatusUpdateSuccessful = Boolean.TRUE;
                LOG.info("[x] Updated status to '" + state + "' on '" + statusUrl + "'");
            } else {
                lastStatusUpdateSuccessful = Boolean.FALSE;
                throw new WorkerException("Could not update status on backend! Returned status: " + putResp.getStatus() + ", backend body: " + putResp.getBody());
            }

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

        WorkerRegister registerObj = new WorkerRegister();
        registerObj.setId(this.workerId);
        
        String[] subscriptionKeys = this.properties.getProperty("graviton.subscription").split(",");
        ArrayList<WorkerRegisterSubscription> subscriptions = new ArrayList<WorkerRegisterSubscription>();
        
        for (String subscriptionKey: subscriptionKeys) {
            WorkerRegisterSubscription subObj = new WorkerRegisterSubscription();
            subObj.setEvent(subscriptionKey);
            subscriptions.add(subObj);
        }
        
        registerObj.setSubscription(subscriptions);
        
        HttpResponse<String> response = 
                Unirest.put(this.properties.getProperty("graviton.registerUrl"))
                .routeParam("workerId", this.workerId)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body(JSON.std.asString(registerObj))
                .asString();

        LOG.info("[*] Worker register response code: " + response.getStatus());

        if (response.getStatus() == 204) {
            isRegistered = Boolean.TRUE;
        } else {
            throw new WorkerException("Could not register worker on backend! Returned status: " + response.getStatus() + ", backend body: " + response.getBody());
        }        
    }    
    
}
