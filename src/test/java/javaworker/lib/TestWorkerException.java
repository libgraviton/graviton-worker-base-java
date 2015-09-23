package javaworker.lib;

import org.gravitonlib.workerbase.WorkerAbstract;
import org.gravitonlib.workerbase.WorkerException;
import org.gravitonlib.workerbase.model.QueueEvent;

public class TestWorkerException extends WorkerAbstract {

    public boolean concerningRequestCalled = false;
    public boolean handleRequestCalled = false;
    public boolean isConcerningRequest = true;
    public boolean throwWorkerException = true;
    
    /**
     * worker logic is implemented here
     * 
     * @param body message body as object
     * 
     * @throws WorkerException
     */
    public void handleRequest(QueueEvent qevent) throws Exception {
        this.handleRequestCalled = true;
        if (this.throwWorkerException) {
            throw new WorkerException("Something bad happened!");
        } else {
            throw new Exception("Another thing happened!");
        }
        
    }
    
    public boolean isConcerningRequest(QueueEvent qevent) {
        return this.isConcerningRequest;
    }
}
