package javaworker.lib;

import org.gravitonlib.workerbase.WorkerAbstract;
import org.gravitonlib.workerbase.WorkerException;
import org.gravitonlib.workerbase.model.QueueEvent;

public class TestWorkerException extends WorkerAbstract {

    public boolean concerningRequestCalled = false;
    public boolean handleRequestCalled = false;
    public boolean throwWorkerException = true;
    public boolean doAutoStuff = true;
    
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
        return true;
    }
    
    public Boolean doAutoUpdateStatus()
    {
        return this.doAutoStuff;
    }
   
    public Boolean doAutoRegister()
    {
        return this.doAutoStuff;
    }
}
