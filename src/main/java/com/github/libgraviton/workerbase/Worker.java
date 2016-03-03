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
import java.util.Arrays;
import java.util.List;
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
     * @throws WorkerException if setup failed.
     */
    public Worker(WorkerAbstract worker) throws WorkerException {
        try {
            loadProperties();
        } catch (IOException e) {
            throw new WorkerException(e);
        }
        worker.initialize(properties);
        worker.onStartUp();
        this.worker = worker;
    }
    
    /**
     * initializes all
     *
     * @throws IOException if running the Worker failed.
     */
    public void run() throws IOException {
        applyVcapConfig();
        connectToQueue();
    }

    /**
     * connects to the queue
     * 
     * @throws java.io.IOException
     */
    private void connectToQueue() throws IOException {
        ConnectionFactory factory = getConnectionFactory();
        factory.setHost(properties.getProperty("queue.host"));
        factory.setPort(Integer.parseInt(properties.getProperty("queue.port")));
        factory.setUsername(properties.getProperty("queue.username"));
        factory.setPassword(properties.getProperty("queue.password"));
        factory.setVirtualHost(properties.getProperty("queue.vhost"));
        factory.setAutomaticRecoveryEnabled(true);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String exchangeName = properties.getProperty("queue.exchangeName");
        List<String> bindKeys = Arrays.asList(properties.getProperty("queue.bindKey").split(","));
        
        channel.exchangeDeclare(exchangeName, "topic", true);
        String queueName = channel.queueDeclare().getQueue();

        for (String bindKey : bindKeys) {
            channel.queueBind(queueName, exchangeName, bindKey);
            LOG.info("Subscribed on topic exchange '" + exchangeName + "' using binding key '" + bindKey + "'");
        }
        LOG.info("Waiting for messages...");

        channel.basicQos(2);
        channel.basicConsume(queueName, true, getWorkerConsumer(channel, worker));
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
    private void loadProperties() throws IOException {
        properties = new Properties();
        // load defaults
        try (InputStream defaultProperties = getClass().getClassLoader().getResourceAsStream("default.properties")) {
            properties.load(defaultProperties);
        }

        // overrides?
        String propertiesPath;
        if (System.getProperty("propFile", "").equals("")) {
            propertiesPath = "etc/app.properties";
        } else {
            propertiesPath = System.getProperty("propFile");
        }

        try (FileInputStream appProperties = new FileInputStream(propertiesPath)) {
            properties.load(appProperties);
        } catch (IOException e) {
            LOG.debug("No overriding properties found at '" + propertiesPath + "'.");
        }

        // let system properties override everything..
        properties.putAll(System.getProperties());

        LOG.info("Loaded app.properties from " + propertiesPath);
    }

    /**
     * Let's see if we have VCAP ENV vars that we should apply to configuration
     *
     */
    private void applyVcapConfig() throws IOException {
        String vcap = this.getVcap();
        if (vcap != null) {
            DeferredMap vcapConf = (DeferredMap) JSON.std.anyFrom(vcap);
            if (vcapConf.containsKey("rabbitmq-3.0")) {
                @SuppressWarnings("unchecked")
                DeferredMap vcapCreds = ((ArrayList<DeferredMap>) vcapConf.get("rabbitmq-3.0")).get(0);
                vcapCreds = (DeferredMap) vcapCreds.get("credentials");

                properties.setProperty("queue.host", vcapCreds.get("host").toString());
                properties.setProperty("queue.port", vcapCreds.get("port").toString());
                properties.setProperty("queue.username", vcapCreds.get("username").toString());
                properties.setProperty("queue.password", vcapCreds.get("password").toString());
                properties.setProperty("queue.vhost", vcapCreds.get("vhost").toString());
            }
        }
    }
    
    /**
     * Gets the properties
     *
     * @return properties
     */
    public Properties getProperties() {
        return properties;
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
