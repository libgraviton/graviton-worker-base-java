package org.gravitonlib.workerbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.gravitonlib.workerbase.model.QueueEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import com.fasterxml.jackson.jr.ob.impl.DeferredMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public abstract class WorkerAbstract {

    /**
     * status constants
     */
    public static final String STATUS_WORKING = "working";
    public static final String STATUS_DONE = "done";
    public static final String STATUS_FAILED = "failed";
    
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
     * 
     * @throws WorkerException
     */
    abstract public void handleRequest(QueueEvent body) throws WorkerException;
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     * 
     * @param body queueevent object
     * 
     * @return boolean true if not, false if yes
     */
    abstract public boolean isConcerningRequest(QueueEvent body);
    
    /**
     * initializes this worker, will be called by the library
     * 
     * @param properties properties
     * @throws Exception
     * 
     * @return void
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
     * @throws IOException
     * 
     * @return void
     */
    public final void handleDelivery(String consumerTag, QueueEvent qevent)
            throws IOException {

        // get status url
        String statusUrl = qevent.getStatus().get$ref();
        String documentUrl = qevent.getDocument().get$ref();

        if (statusUrl == null || documentUrl == null) {
            return;
        }
        
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
            
        } catch (WorkerException e) {
            System.out.println("Error in worker: " + e.getMessage());
            e.printStackTrace();

            if (this.doAutoUpdateStatus()) {
                this.setStatus(statusUrl, STATUS_FAILED, e.getMessage());
                System.out.println(" [x] LIB Updated status to 'failed' on '" + statusUrl + "'");
            }
            
        } catch (Exception e) {
            System.out.println("General error in logic: " + e.getMessage());
            e.printStackTrace();
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
     * convenience function to set the status
     * 
     * @param statusUrl status url 
     * @param status which status
     * 
     * @return void
     */
    protected void setStatus(String statusUrl, String status) {
        this.setStatus(statusUrl, status, "");
    }
    
    /**
     * sets the status to our backend
     * 
     * @param statusUrl url to status document
     * @param status status we set to
     * @param errorInformation error information
     * 
     * @return void
     */
    @SuppressWarnings("unchecked")
    protected void setStatus(String statusUrl, String status, String errorInformation) {
        try {
            HttpResponse<String> response = Unirest.get(statusUrl).header("Accept", "application/json").asString();
            
            DeferredMap ob = (DeferredMap) JSON.std.anyFrom(response.getBody());

            ArrayList<DeferredMap> statusObj = (ArrayList<DeferredMap>) ob.get("status");

            // modify our status in the status array
            for (int i = 0; i < statusObj.size(); i++) {
                DeferredMap statusEntry = statusObj.get(i);
                if (statusEntry.get("workerId").toString().equals(this.workerId)) {
                    statusEntry.put("status", status);
                }
                statusObj.set(i, statusEntry);
            }

            ob.put("status", statusObj);
            
            // error information?
            if (errorInformation.length() > 0) {
                DeferredMap errorObj = new DeferredMap(false);
                errorObj.put("workerId", this.workerId);
                errorObj.put("content", errorInformation);
                
                // add or create list?
                if (ob.get("errorInformation") instanceof ArrayList<?>) {
                    ((ArrayList<DeferredMap>) ob.get("errorInformation")).add(errorObj);
                } else {
                    ArrayList<DeferredMap> errorList = new ArrayList<DeferredMap>();
                    errorList.add(errorObj);
                    ob.put("errorInformation", errorList);
                }
            }

            // send the new status to the backend
            Unirest.put(statusUrl).header("Content-Type", "application/json").body(JSON.std.asString(ob)).asString();

        } catch (UnirestException e) {
            System.out.println("Error GETting Status resource: " + e.getMessage());
            e.printStackTrace();
        } catch (JSONObjectException e) {
            System.out.println("Error Deserializing JSON: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Network error on Status update: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Different exception on status set! " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * gets a $ref of an object of eventstatus
     * 
     * @param map map 
     * @param property which property
     * 
     * @return the $ref value
     */
    protected String getResponseRefPart(DeferredMap map, String property) {
        String url = null;
        if (map.containsKey(property)) {
            Object subobj = map.get(property);
            if (subobj instanceof DeferredMap && ((DeferredMap) subobj).containsKey("$ref")) {
                url = ((DeferredMap) subobj).get("$ref").toString();
            }
        }
        return url;
    }

    /**
     * registers our worker with the backend
     * @throws UnirestException
     * @throws JSONObjectException
     * @throws JsonProcessingException
     * @throws IOException
     * 
     * @return void
     */
    protected void registerWorker() throws UnirestException, JSONObjectException, JsonProcessingException, IOException {

        ArrayComposer<ObjectComposer<JSONComposer<String>>> preRegister = JSON
                .std
                .composeString()
                .startObject()
                    .put("id", this.workerId)
                    .startArrayField("subscription");
        
        String[] subscriptionKeys = this.properties.getProperty("graviton.subscription").split(",");
        for (String subscriptionKey: subscriptionKeys) {
            preRegister = preRegister
                .startObject()
                .put("event", subscriptionKey)
            .end();
        }
        
        String register = preRegister
                .end()
                .end()
                .finish();
        
        HttpResponse<JsonNode> jsonResponse = Unirest.put(this.properties.getProperty("graviton.registerUrl"))
                .routeParam("workerId", this.workerId).header("Content-Type", "application/json").body(register)
                .asJson();

        System.out.println(" [*] Worker register response code: " + jsonResponse.getStatus());
    }    
    
}
