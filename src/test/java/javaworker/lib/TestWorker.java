package javaworker.lib;

import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

public class TestWorker extends WorkerAbstract {

    public boolean concerningRequestCalled = false;

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
    public boolean isConcerningRequest(QueueEvent qevent) {
        this.concerningRequestCalled = true;
        return true;
    }

    public QueueEvent getHandledQueueEvent() {
        return handledQueueEvent;
    }
}
