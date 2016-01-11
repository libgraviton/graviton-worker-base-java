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

/**
 * <p>Abstract WorkerAbstract class.</p>
 *
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 * @version $Id: $Id
 */
public abstract class WorkerAbstract {

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
            this.setStatus(statusUrl, STATUS_WORKING);
            System.out.println(" [x] LIB: Updated status to 'working' on '" + statusUrl + "'");
        }

        try {
            // call the worker
            this.handleRequest(qevent);
            
            if (this.doAutoUpdateStatus()) {
                this.setStatus(statusUrl, STATUS_DONE);
                System.out.println(" [x] LIB Updated status to 'done' on '" + statusUrl + "'");
            }
            
        } catch (Exception e) {
            System.out.println("Error in worker: " + e.toString());
            e.printStackTrace();

            if (this.doAutoUpdateStatus()) {
                this.setStatus(statusUrl, STATUS_FAILED, e.toString());
                System.out.println(" [x] LIB Updated status to 'failed' on '" + statusUrl + "'");
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
     * convenience function to set the status
     *
     * @param statusUrl status url
     * @param statusUrl status url
     * @param status which status
     */
    protected void setStatus(String statusUrl, String status) {
        this.setStatus(statusUrl, status, "");
    }
    
    /**
     * Set status with a string based error information
     *
     * @param statusUrl url to status document
     * @param statusUrl url to status document
     * @param status status we set to
     * @param errorInformation error information message
     */
    protected void setStatus(String statusUrl, String status, String errorInformation) {
        
        EventStatusInformation infoObj = null;
        
        if (errorInformation.length() > 0) {
            infoObj = new EventStatusInformation();
            infoObj.setWorkerId(this.workerId);
            infoObj.setType(INFORMATION_TYPE_ERROR);
            infoObj.setContent(errorInformation);
        }
        
        this.setStatus(statusUrl, status, infoObj);
    }
    
    /**
     * sets the status to our backend
     *
     * @param statusUrl url to status document
     * @param statusUrl url to status document
     * @param status status we set to
     * @param informationEntry an EventStatusInformation instance that will be added to the information array
     */
    protected void setStatus(String statusUrl, String status, EventStatusInformation informationEntry) {
        try {
            HttpResponse<String> response = Unirest.get(statusUrl).header("Accept", "application/json").asString();
            
            EventStatus eventStatus = JSON.std.beanFrom(EventStatus.class, response.getBody());

            // modify our status in the status array
            ArrayList<EventStatusStatus> statusObj = eventStatus.getStatus();
            for (int i = 0; i < statusObj.size(); i++) {
                EventStatusStatus statusEntry = statusObj.get(i);
                if (statusEntry.getWorkerId().equals(this.workerId)) {
                    statusEntry.setStatus(status);
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
            
            if (putResp.getStatus() != 204) {
                throw new WorkerException("Could not update status on backend! Returned status: " + putResp.getStatus() + ", backend body: " + putResp.getBody());
            }

        } catch (WorkerException e) {
            System.out.println(" [F] Backend error on status update! " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(" [F] Exception on status set! " + e.getMessage());
            e.printStackTrace();
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

        System.out.println(" [*] Worker register response code: " + response.getStatus());
        
        if (response.getStatus() != 204) {
            throw new WorkerException("Could register worker on backend! Returned status: " + response.getStatus() + ", backend body: " + response.getBody());
        }        
    }    
    
}
