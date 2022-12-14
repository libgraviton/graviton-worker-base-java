package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.workerbase.QueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.model.QueueEvent;
import io.activej.inject.annotation.Inject;

@GravitonWorker
public class TestQueueWorker extends QueueWorkerAbstract {

    public boolean shouldHandleRequestCalled = false;

    public boolean hasStartedUp = false;

    protected QueueEvent handledQueueEvent;

    @Inject
    public TestQueueWorker(WorkerScope workerScope) {
        super(workerScope);
    }

    /**
     * worker logic is implemented here
     * 
     * @param qevent queue event from request
     *
     */
    public void handleRequest(QueueEvent qevent, QueueEventScope queueEventScope) {
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

    public void onStartUp() throws WorkerException {
        hasStartedUp = true;
    }
}
