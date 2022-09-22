/**
 * connects to the queue and subscribes the WorkerConsumer on the queue
 */

package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumer;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.util.PrometheusServer;
import io.activej.inject.annotation.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

/**
 * <p>Worker class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @version $Id: $Id
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 */
public class Worker {

    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

    private final Properties properties;

    @Provides
    WorkerInterface getWorker(Properties properties) throws Exception {
        worker.initialize(properties);
        worker.onStartUp();
        return worker;
    }

    /**
     * worker
     */
    private WorkerInterface worker;

    private Worker(WorkerInterface worker) throws Exception {
        // init di!
        DependencyInjection.init(worker, List.of(this));

        properties = DependencyInjection.getInjector().getInstance(Properties.class);

        LOG.info(
                "Starting up '{}' version '{}'. Java runtime '{}', version '{}', TZ '{}'",
                properties.getProperty("application.name"),
                properties.getProperty("application.version"),
                System.getProperty("java.runtime.name"),
                System.getProperty("java.runtime.version"),
                System.getProperty("user.timezone")
        );

        // put it there..
        this.worker = worker;

        // get it back from di!
        this.worker = DependencyInjection.getInjector().getInstance(WorkerInterface.class);
    }

    /**
     * constructor for "normal" queue based workers..
     *
     * @param worker worker instance
     * @throws WorkerException if setup failed.
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     */
    public Worker(QueueWorkerAbstract worker) throws Exception {
        this((WorkerInterface) worker);
    }

    /**
     * constructor for standalone workers
     *
     * @param worker worker instance
     * @throws WorkerException                if setup failed.
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     */
    public Worker(StandaloneWorker worker) throws Exception {
        this((WorkerInterface) worker);
        // call this here as this.run is not called(?) by standalone
        new PrometheusServer(this.worker.getWorkerId(), properties);
        ((StandaloneWorker) this.worker).run();
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

        assert (worker instanceof QueueWorkerAbstract);

        new PrometheusServer(worker.getWorkerId(), properties);

        QueueManager queueManager = DependencyInjection.getInjector().getInstance(QueueManager.class);
        try {
            queueManager.connect((QueueWorkerInterface) worker);
        } catch (CannotConnectToQueue | CannotRegisterConsumer e) {
            throw new WorkerException("Unable to initialize worker.", e);
        }
    }
}
