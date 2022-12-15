package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.util.PrometheusServer;
import io.activej.inject.annotation.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * <p>Worker class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @version $Id: $Id
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 */
@GravitonWorkerDiScan
final class WorkerLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerLauncher.class);

    private final WorkerInterface worker;
    private final PrometheusServer prometheusServer;
    private final QueueWorkerRunner queueWorkerRunner;

    @Inject
    public WorkerLauncher(
            WorkerInterface worker,
            Properties properties
    ) {

        this.worker = worker;

        String applicationName = properties.getProperty("application.name");
        if (properties.getProperty("graviton.workerName") != null) {
            applicationName = properties.getProperty("graviton.workerName");
        }

        LOG.info(
                "Starting '{} {}' (worker-base '{}'). Runtime '{}' version '{}', TZ '{}'",
                applicationName,
                properties.getProperty("application.version"),
                WorkerUtil.getWorkerBaseVersion(),
                System.getProperty("java.runtime.name"),
                System.getProperty("java.runtime.version"),
                System.getProperty("user.timezone")
        );

        prometheusServer = new PrometheusServer(worker.getWorkerId());

        if (worker instanceof QueueWorkerAbstract) {
            queueWorkerRunner = new QueueWorkerRunner((QueueWorkerAbstract) worker);
        } else {
            queueWorkerRunner = null;
        }
    }

    public WorkerInterface getWorker() {
        return worker;
    }

    public QueueWorkerRunner getQueueWorkerRunner() {
        return queueWorkerRunner;
    }

    /**
     * setup worker
     *
     * @throws WorkerException If connecting to queue is impossible
     */
    public void run() throws WorkerException {
        if (worker == null) {
            throw new RuntimeException("No worker set to be run...");
        }

        // on startup call
        worker.onStartUp();

        if (worker instanceof StandaloneWorker) {
            ((StandaloneWorker) this.worker).run();
        } else if (worker instanceof QueueWorkerAbstract && queueWorkerRunner != null) {
            queueWorkerRunner.run();
        } else {
            throw new RuntimeException("Could not find any runnable worker!");
        }
    }

    public void stop() {
        prometheusServer.stop();
        if (queueWorkerRunner != null) {
            queueWorkerRunner.close();
        }
    }
}
