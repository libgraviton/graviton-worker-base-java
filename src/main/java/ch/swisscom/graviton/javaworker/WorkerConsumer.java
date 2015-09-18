package ch.swisscom.graviton.javaworker;

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
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class WorkerConsumer extends DefaultConsumer {
    
    private String workerId;
    private Properties properties;

    public WorkerConsumer(Channel channel, Properties properties) {
        super(channel);
        
        this.properties = properties;
        this.workerId = this.properties.getProperty("graviton.workerId");
        
        try {
            this.registerWorker();
        } catch (IOException e) {
            System.out.println("Problem connecting to the queue: " + e.getMessage());
            e.printStackTrace();
        } catch (UnirestException e) {
            System.out.println("Failed to register worker: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
        
        // deserialize
        DeferredMap ob = (DeferredMap) JSON.std.anyFrom(message);
        
        // get status url
        String statusUrl = this.getResponseRefPart(ob, "status");
        String documentUrl = this.getResponseRefPart(ob, "document");
        
        if (statusUrl == null || documentUrl == null) {
            return;
        }

        this.setStatus(statusUrl, "working");
        System.out.println(" [x] Updated status to 'working' on '" + statusUrl + "'");
        
        try {
            this.sendToHipchat(ob.get("event").toString(), statusUrl, documentUrl);
        } catch (Exception e) {
            System.out.println("Error sending POST to HipChat: " + e.getMessage());
            e.printStackTrace();
        }
        
        this.setStatus(statusUrl, "done");
        
        // send ACK to queue
        this.getChannel().basicAck(envelope.getDeliveryTag(), false);
        
        System.out.println(" [x] Updated status to 'done' on '" + statusUrl + "'");
    }
    
    private void setStatus(String statusUrl, String status) {
        try {
            HttpResponse<String> response = Unirest.get(statusUrl)
                    .header("Accept", "application/json")
                    .asString();
            
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
            Unirest.put(statusUrl)
                    .header("Content-Type", "application/json")
                    .body(JSON.std.asString(ob))
                    .asString();
            
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
    
    private String getResponseRefPart(DeferredMap map, String property) {
        String url = null;
        if (map.containsKey(property)) {
            Object subobj = map.get(property);
            if (subobj instanceof DeferredMap && ((DeferredMap) subobj).containsKey("$ref")) {
                url = ((DeferredMap) subobj).get("$ref").toString();
            }
        }   
        return url;
    }
    
    private void sendToHipchat(String event, String statusUrl, String documentUrl) throws Exception {
        
        String body = "(Java worker) I received an event on <i>" + event + "</i>. The status I updated is " + statusUrl + " - "
                + "the affected document lies at " + documentUrl;

        String hipchat = JSON.std
                .composeString()
                .startObject()
                .put("color", "yellow")
                .put("message", body)
                .put("notify", false)
                .end()
                .finish();
        
        HttpResponse<String> jsonResponse = Unirest.post(this.properties.getProperty("graviton.hipchatUrl"))
                .header("Content-Type", "application/json")
                .body(hipchat)
                .asString();        
        
        System.out.println(" [*] Hipchat POST response code: " + jsonResponse.getStatus());
    }
    
    private void registerWorker() throws UnirestException, JSONObjectException, JsonProcessingException, IOException {
        
        String register = JSON.std
                .composeString()
                .startObject()
                .put("id", "java-test")
                .startArrayField("subscription")
                    .startObject()
                    .put("event", this.properties.getProperty("graviton.subscription"))
                    .end()
                .end()
                .end()
                .finish();
        
        HttpResponse<JsonNode> jsonResponse = Unirest.put(this.properties.getProperty("graviton.registerUrl"))
                .routeParam("workerId", this.workerId)
                .header("Content-Type", "application/json")
                .body(register)
                .asJson();
        
        System.out.println(" [*] Worker register response code: " + jsonResponse.getStatus());
    }    

}
