package ch.swisscom.graviton.javaworker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.impl.DeferredMap;
import com.rabbitmq.client.*;

/**
 * Created by dn on 17.09.15.
 */
public class Worker {

    private static final String EXCHANGE_NAME = "graviton";
    
    private Properties properties;

    public void run() {
        
        try {
            this.loadProperties();
            this.applyVcapConfig();
            this.connectToQueue();
        } catch (IOException e) {
            System.out.println("Problem connecting to the queue: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Could not parse VCAP configuration: " + e.getMessage());
            e.printStackTrace();            
        }
    }
    
    /**
     * @throws java.io.IOException
     */
    private void connectToQueue() throws IOException {
        
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.properties.getProperty("queue.host"));
        factory.setPort(Integer.parseInt(this.properties.getProperty("queue.port")));
        factory.setUsername(this.properties.getProperty("queue.username"));
        factory.setPassword(this.properties.getProperty("queue.password"));
        factory.setVirtualHost(this.properties.getProperty("queue.vhost"));
        
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName, EXCHANGE_NAME, "document.core.app.*");

        System.out.println(" [*] Waiting for messages.");
        
        WorkerConsumer consumer = new WorkerConsumer(channel, this.properties);
        
        channel.basicQos(2);
        channel.basicConsume(queueName, false, consumer);
    }    
    
    private void loadProperties() {
        this.properties = new Properties();
        try {
            this.properties.load(ClassLoader.getSystemResourceAsStream("properties.properties"));
        } catch (Exception e1) {
            System.out.println("Could not load properties: " + e1.getMessage());
            e1.printStackTrace();
        }        
    }
    
    /**
     * Let's see if we have VCAP ENV vars that we should apply to configuration
     * 
     * @return void
     * @throws IOException 
     * @throws JSONObjectException 
     */
    private void applyVcapConfig() throws Exception {
        String vcap = System.getenv("VCAP_SERVICES");
        if (vcap != null) {
            DeferredMap vcapConf = (DeferredMap) JSON.std.anyFrom(vcap);
            if (vcapConf.containsKey("rabbitmq-3.0")) {
                @SuppressWarnings("unchecked")
                DeferredMap vcapCreds = (DeferredMap) ((ArrayList<DeferredMap>) vcapConf.get("rabbitmq-3.0")).get(0);
                vcapCreds = (DeferredMap) vcapCreds.get("credentials");

                this.properties.setProperty("queue.host", vcapCreds.get("host").toString());
                this.properties.setProperty("queue.port", vcapCreds.get("port").toString());
                this.properties.setProperty("queue.username", vcapCreds.get("username").toString());
                this.properties.setProperty("queue.password", vcapCreds.get("password").toString());
                this.properties.setProperty("queue.vhost", vcapCreds.get("vhost").toString());
            }
        }
    }
}
