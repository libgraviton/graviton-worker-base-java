package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumer;
import io.activej.inject.annotation.Inject;

@GravitonWorkerDiScan
public class QueueWorkerRunner {

    private final QueueManager queueManager;

    @Inject
    public QueueWorkerRunner(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public void run(QueueWorkerAbstract worker) throws WorkerException {
        try {
            queueManager.connect(worker);
        } catch (CannotConnectToQueue | CannotRegisterConsumer e) {
            throw new WorkerException("Unable to initialize worker.", e);
        }
    }

}
