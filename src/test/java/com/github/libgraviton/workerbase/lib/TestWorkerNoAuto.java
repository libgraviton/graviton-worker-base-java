package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

public class TestWorkerNoAuto extends WorkerAbstract {

    public boolean concerningRequestCalled = false;
    public boolean handleRequestCalled = false;
    public boolean isConcerningRequest = true;
    
    /**
     * worker logic is implemented here
     *
     * @throws WorkerException
     */
    public void handleRequest(QueueEvent qevent) throws WorkerException {
        this.handleRequestCalled = true;
    }
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     *
     * @return boolean true if not, false if yes
     */
    public boolean shouldHandleRequest(QueueEvent qevent) {
        this.concerningRequestCalled = true;
        return this.isConcerningRequest;
    }
    
    public Boolean shouldAutoUpdateStatus()
    {
        return false;
    }
    
    public Boolean shouldAutoRegister()
    {
        return false;
    }

    protected GravitonApi initGravitonApi() {
        return gravitonApi;
    }
    
}
