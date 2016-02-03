/**
 * connects to the queue and subscribes the WorkerConsumer on the queue
 */

package com.github.libgraviton.workerbase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;
import com.fasterxml.jackson.jr.ob.impl.DeferredMap;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Worker class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class Worker {

    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

    /**
     * properties
     */
    private Properties properties;
    
    /**
     * worker
     */
    private WorkerAbstract worker;
    
    /**
     * constructor
     *
     * @param worker worker instance
     * @throws java.lang.Exception if any.
     */
    public Worker(WorkerAbstract worker) throws Exception {
        this.loadProperties();
        worker.initialize(properties);
        worker.onStartUp();
        this.worker = worker;
    }
    
    /**
     * initializes all
     *
     * @throws java.lang.Exception if any.
     */
    public void run() throws Exception {
        this.applyVcapConfig();
        this.connectToQueue();
    }

    /**
     * connects to the queue
     * 
     * @throws java.io.IOException
     */
    private void connectToQueue() throws IOException {

        ConnectionFactory factory = this.getConnectionFactory();
        factory.setHost(this.properties.getProperty("queue.host"));
        factory.setPort(Integer.parseInt(this.properties.getProperty("queue.port")));
        factory.setUsername(this.properties.getProperty("queue.username"));
        factory.setPassword(this.properties.getProperty("queue.password"));
        factory.setVirtualHost(this.properties.getProperty("queue.vhost"));
        factory.setAutomaticRecoveryEnabled(true);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String exchangeName = this.properties.getProperty("queue.exchangeName");
        String[] bindKeys = this.properties.getProperty("queue.bindKey").split(",");
        
        channel.exchangeDeclare(exchangeName, "topic", true);
        String queueName = channel.queueDeclare().getQueue();

        for (String bindKey : bindKeys) {
            bindKey = bindKey.trim();
            channel.queueBind(queueName, exchangeName, bindKey);
            LOG.info("[*] Subscribed on topic exchange '" + exchangeName + "' using binding key '" + bindKey);
        }
        LOG.info("[*] Waiting for messages...");

        channel.basicQos(2);
        channel.basicConsume(queueName, true, this.getWorkerConsumer(channel, worker));
    }
    
    /**
     * function to return the connection factory
     *
     * @return connection factory
     */
    public ConnectionFactory getConnectionFactory()
    {
        return new ConnectionFactory();
    }
    
    /**
     * return our WorkerConsumer
     *
     * @param channel rabbitmq channel
     * @param worker the worker
     * @return worker consumer
     */
    public WorkerConsumer getWorkerConsumer(Channel channel, WorkerAbstract worker) {
        return new WorkerConsumer(channel, worker);
    }

    /**
     * loads the properties
     */
    private void loadProperties() {
        this.properties = new Properties();
        try {
            
            // load defaults
            InputStream defaultProps = this.getClass().getClassLoader().getResourceAsStream("default.properties");
            this.properties.load(defaultProps);
            defaultProps.close();
            
            // overrides?
            try {
            
                String propertiesPath;
                if (System.getProperty("propFile", "").equals("")) {
                    String currentPath = Worker.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                    String decodedPath = URLDecoder.decode(currentPath, "UTF-8");
                    String propertiesBasename = FilenameUtils.getFullPath(decodedPath);
                    propertiesPath = propertiesBasename + "app.properties";
                } else {
                    propertiesPath = System.getProperty("propFile");
                }
                
                FileInputStream appProps = new FileInputStream(propertiesPath);
                this.properties.load(appProps);
                appProps.close();

                // let system properties override everything..
                this.properties.putAll(System.getProperties());

                LOG.info(" [*] Loaded app.properties from " + propertiesPath);
                
            } catch (FileNotFoundException e) {
                // no problem..
            }
            
        } catch (Exception e1) {
            LOG.error("Could not load properties", e1);
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
        String vcap = this.getVcap();
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
    
    /**
     * Gets the properties
     *
     * @return properties
     */
    public Properties getProperties() {
        return this.properties;
    }
    
    /**
     * Getter for vcap config
     *
     * @return vcap variable
     */
    public String getVcap() {
        return System.getenv("VCAP_SERVICES");
    }
}
