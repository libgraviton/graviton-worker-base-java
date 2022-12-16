package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.workerbase.QueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import io.activej.inject.annotation.Inject;

@GravitonWorker
public class TestQueueWorkerNoAuto extends QueueWorkerAbstract {

    public boolean concerningRequestCalled = false;
    public boolean handleRequestCalled = false;
    public boolean isConcerningRequest = true;

    @Inject
    public TestQueueWorkerNoAuto(WorkerScope workerScope) {
        super(workerScope);
    }

    /**
     * worker logic is implemented here
     *
     * @throws WorkerException
     */
    public void handleRequest(QueueEventScope queueEventScope) {
        this.handleRequestCalled = true;
    }
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     *
     * @return boolean true if not, false if yes
     */
    public boolean shouldHandleRequest(QueueEventScope queueEventScope) {
        this.concerningRequestCalled = true;
        return this.isConcerningRequest;
    }
    
    public boolean shouldAutoUpdateStatus()
    {
        return false;
    }
    
    public boolean shouldAutoRegister()
    {
        return false;
    }

    @Override
    public void onStartUp() {

    }
}
