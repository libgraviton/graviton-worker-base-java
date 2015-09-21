package ch.swisscom.graviton.javaworker.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.impl.DeferredMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public abstract class WorkerAbstract {

    protected Properties properties;
    
    protected String workerId;
        
    /**
     * worker logic is implemented here
     * 
     * @param body message body as object
     * 
     * @throws WorkerException
     */
    abstract public void handleRequest(DeferredMap body) throws WorkerException;
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     * 
     * @param body message body as object
     * 
     * @return boolean true if not, false if yes
     */
    abstract public boolean isConcerningRequest(DeferredMap body);
    
    public final void initialize(Properties properties) throws Exception {
        this.properties = properties;
        this.workerId = this.properties.getProperty("graviton.workerId");

        if (this.doAutoRegister()) this.registerWorker();
    }
        
    public final void handleDelivery(String consumerTag, DeferredMap ob)
            throws IOException {

        // get status url
        String statusUrl = this.getResponseRefPart(ob, "status");
        String documentUrl = this.getResponseRefPart(ob, "document");

        if (statusUrl == null || documentUrl == null) {
            return;
        }
        
        if (this.isConcerningRequest(ob) == false) {
            return;
        }

        if (this.doAutoUpdateStatus()) {
            this.setStatus(statusUrl, "working");
            System.out.println(" [x] LIB: Updated status to 'working' on '" + statusUrl + "'");
        }

        try {
            // call the worker
            this.handleRequest(ob);
        } catch (WorkerException e) {
            System.out.println("Error in worker: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("General error in logic: " + e.getMessage());
            e.printStackTrace();
        }

        if (this.doAutoUpdateStatus()) {
            this.setStatus(statusUrl, "done");
            System.out.println(" [x] LIB Updated status to 'done' on '" + statusUrl + "'");
        }
    }    
    
    public Boolean doAutoUpdateStatus()
    {
        return true;
    }
    
    public Boolean doAutoRegister()
    {
        return true;
    }
    
    /**
     * sets the status to our backend
     * 
     * @param statusUrl url to status document
     * @param status status we set to
     * 
     * @return void
     */
    protected void setStatus(String statusUrl, String status) {
        try {
            HttpResponse<String> response = Unirest.get(statusUrl).header("Accept", "application/json").asString();

            DeferredMap ob = (DeferredMap) JSON.std.anyFrom(response.getBody());

            @SuppressWarnings("unchecked")
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

        String register = JSON.std.composeString().startObject().put("id", "java-test").startArrayField("subscription")
                .startObject().put("event", this.properties.getProperty("graviton.subscription")).end().end().end()
                .finish();

        HttpResponse<JsonNode> jsonResponse = Unirest.put(this.properties.getProperty("graviton.registerUrl"))
                .routeParam("workerId", this.workerId).header("Content-Type", "application/json").body(register)
                .asJson();

        System.out.println(" [*] Worker register response code: " + jsonResponse.getStatus());
    }    
    
}
