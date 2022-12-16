package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.workerbase.QueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import io.activej.inject.annotation.Inject;

@GravitonWorker
public class TestQueueWorkerNoRetryOnException extends QueueWorkerAbstract {

    public int callCount = 0;

    @Inject
    public TestQueueWorkerNoRetryOnException(WorkerScope workerScope) {
        super(workerScope);
    }

    /**
     * worker logic is implemented here
     *
     * @throws WorkerException
     */
    public void handleRequest(QueueEventScope queueEventScope) throws WorkerException {
        callCount++;
        throw new WorkerException("Not good!");
    }
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     *
     * @return boolean true if not, false if yes
     */
    public boolean shouldHandleRequest(QueueEventScope queueEventScope) {
        return true;
    }

    @Override
    public boolean shouldAutoAcknowledgeOnException()
    {
        return true;
    }

    @Override
    public void onStartUp() {

    }
}
