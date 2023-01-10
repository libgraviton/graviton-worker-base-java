package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.workerbase.QueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import io.activej.inject.annotation.Inject;

@GravitonWorkerDiScan
public class TestQueueWorkerException extends QueueWorkerAbstract {

    public boolean handleRequestCalled = false;
    public boolean throwWorkerException = true;
    public boolean doAutoStuff = true;

    @Inject
    public TestQueueWorkerException(WorkerScope workerScope) {
        super(workerScope);
    }

    /**
     * worker logic is implemented here
     *
     * @throws WorkerException
     */
    public void handleRequest(QueueEventScope queueEventScope) throws WorkerException {
        this.handleRequestCalled = true;
        if (this.throwWorkerException) {
            throw new WorkerException("Something bad happened!");
        } else {
            throw new IllegalArgumentException("Another thing happened!");
        }
    }
    
    public boolean shouldHandleRequest(QueueEventScope queueEventScope) {
        return true;
    }
    
    public boolean shouldAutoUpdateStatus()
    {
        return this.doAutoStuff;
    }
   
    public boolean shouldAutoRegister()
    {
        return this.doAutoStuff;
    }

    @Override
    public void onStartUp() {

    }
}
