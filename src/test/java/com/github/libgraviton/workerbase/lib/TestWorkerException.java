package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.workerbase.gdk.GravitonAuthApi;
import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

public class TestWorkerException extends WorkerAbstract {

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
    
    public Boolean shouldAutoUpdateStatus()
    {
        return this.doAutoStuff;
    }
   
    public Boolean shouldAutoRegister()
    {
        return this.doAutoStuff;
    }

    protected GravitonAuthApi initGravitonApi() {
        return gravitonApi;
    }
}
