/**
 * connects to the queue and subscribes the WorkerConsumer on the queue
 */

package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.impl.DeferredMap;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.PropertiesLoader;
import com.github.libgraviton.workerbase.mq.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.mq.QueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

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
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     */
    public Worker(WorkerAbstract worker) throws WorkerException, GravitonCommunicationException {
        try {
            properties = PropertiesLoader.load();
        } catch (IOException e) {
            throw new WorkerException(e);
        }
        worker.initialize(properties);
        worker.onStartUp();
        this.worker = worker;
    }
    
    /**
     * setup worker
     */
    public void run() throws WorkerException {
        try {
            applyVcapConfig();
        } catch (IOException e) {
            LOG.warn("Unable to load vcap config. Skip this step.");
        }
        QueueManager queueManager = worker.getQueueManager();
        try {
            queueManager.connect(worker);
        } catch (CannotConnectToQueue e) {
            throw new WorkerException("Unable to initialize worker.", e);
        }
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
