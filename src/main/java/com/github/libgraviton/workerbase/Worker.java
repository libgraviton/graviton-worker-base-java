/**
 * connects to the queue and subscribes the WorkerConsumer on the queue
 */

package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumer;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.PropertiesLoader;
import io.prometheus.client.exporter.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    private final Properties properties;
    
    /**
     * worker
     */
    private WorkerAbstract worker;

    public Worker(WorkerInterface worker) throws Exception {
        try {
            properties = PropertiesLoader.load(worker);
        } catch (IOException e) {
            throw new WorkerException(e);
        }

        LOG.info("Starting " + properties.getProperty("application.name") + " " + properties.getProperty("application.version"));

        worker.initialize(properties);
        worker.onStartUp();
    }

    /**
     * constructor for "normal" queue based workers..
     *
     * @param worker worker instance
     * @throws WorkerException if setup failed.
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     */
    public Worker(WorkerAbstract worker) throws Exception {
        this((WorkerInterface) worker);
        this.worker = worker;
    }

    /**
     * constructor for standalone workers
     *
     * @param worker worker instance
     * @throws WorkerException if setup failed.
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     */
    public Worker(StandaloneWorker worker) throws Exception {
        this((WorkerInterface) worker);
        initPrometheus();
        worker.run();
    }

    private void initWorker(WorkerInterface worker) throws Exception {
        worker.initialize(properties);
        worker.onStartUp();
    }

    /**
     * initializes our simple httpserver for prom metrics
     *
     * @throws IOException
     */
    private void initPrometheus() {
        try {
            HTTPServer server = new HTTPServer(9999);
            LOG.info("Started prometheus HTTPServer for metrics on http://*:9999/metrics");
        } catch (Throwable t) {
            LOG.error("Could not start prometheus metrics HTTPServer", t);
        }
    }
    
    /**
     * setup worker
     *
     * @throws WorkerException If connecting to queue is impossible
     */
    public void run() throws Exception {
        if (worker == null) {
            throw new WorkerException("No worker set to be run in the traditional way...");
        }

        initPrometheus();

        QueueManager queueManager = worker.getQueueManager();
        try {
            queueManager.connect(worker);
        } catch (CannotConnectToQueue | CannotRegisterConsumer e) {
            throw new WorkerException("Unable to initialize worker.", e);
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
}
