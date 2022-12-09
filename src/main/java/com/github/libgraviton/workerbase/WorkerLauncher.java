/**
 * connects to the queue and subscribes the WorkerConsumer on the queue
 */

package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumer;
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

    @Inject
    public WorkerLauncher(
            WorkerInterface worker,
            Properties properties
    ) throws WorkerException {
        // init di!
        //DependencyInjection.init(List.of());

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
    }

    /**
     * setup worker
     *
     * @throws WorkerException If connecting to queue is impossible
     */
    public void run() throws WorkerException {
        if (worker == null) {
            throw new WorkerException("No worker set to be run...");
        }

        new PrometheusServer(worker.getWorkerId());

        if (worker instanceof StandaloneWorker) {
            ((StandaloneWorker) this.worker).run();
        }

        if (worker instanceof QueueWorkerAbstract) {
            QueueWorkerRunner queueWorkerRunner = DependencyInjection.getInstance(QueueWorkerRunner.class);
            queueWorkerRunner.run((QueueWorkerAbstract) worker);
        }
    }
}
