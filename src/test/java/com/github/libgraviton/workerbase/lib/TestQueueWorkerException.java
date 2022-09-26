package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.workerbase.QueueWorkerAbstract;
import com.github.libgraviton.workerbase.gdk.GravitonAuthApi;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

public class TestQueueWorkerException extends QueueWorkerAbstract {

    public boolean handleRequestCalled = false;
    public boolean throwWorkerException = true;
    public boolean doAutoStuff = true;
    
    /**
     * worker logic is implemented here
     * 
     * @param queueEvent message body as object
     * 
     * @throws WorkerException
     */
    public void handleRequest(QueueEvent queueEvent) throws WorkerException {
        this.handleRequestCalled = true;
        if (this.throwWorkerException) {
            throw new WorkerException("Something bad happened!");
        } else {
            throw new IllegalArgumentException("Another thing happened!");
        }
    }
    
    public boolean shouldHandleRequest(QueueEvent qevent) {
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
}
