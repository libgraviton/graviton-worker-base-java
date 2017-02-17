package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

public class TestWorker extends WorkerAbstract {

    public boolean shouldHandleRequestCalled = false;

    protected QueueEvent handledQueueEvent;
    
    /**
     * worker logic is implemented here
     * 
     * @param qevent queue event from request
     * 
     * @throws WorkerException
     */
    public void handleRequest(QueueEvent qevent) throws WorkerException {
        handledQueueEvent = qevent;
    }

    /**

     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     * 
     * @param qevent queue event from request
     * 
     * @return boolean true if not, false if yes
     */
    public boolean shouldHandleRequest(QueueEvent qevent) {
        this.shouldHandleRequestCalled = true;
        return true;
    }

    public QueueEvent getHandledQueueEvent() {
        return handledQueueEvent;
    }

    protected GravitonApi initGravitonApi() {
        return null;
    }
}
