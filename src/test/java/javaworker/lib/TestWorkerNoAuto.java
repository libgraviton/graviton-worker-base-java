package javaworker.lib;

import org.gravitonlib.workerbase.WorkerAbstract;
import org.gravitonlib.workerbase.WorkerException;
import org.gravitonlib.workerbase.model.QueueEvent;

public class TestWorkerNoAuto extends WorkerAbstract {

    public boolean concerningRequestCalled = false;
    public boolean handleRequestCalled = false;
    public boolean isConcerningRequest = true;
    
    /**
     * worker logic is implemented here
     * 
     * @param body message body as object
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
     * @param body message body as object
     * 
     * @return boolean true if not, false if yes
     */
    public boolean isConcerningRequest(QueueEvent qevent) {
        this.concerningRequestCalled = true;
        return this.isConcerningRequest;
    }
    
    public Boolean doAutoUpdateStatus()
    {
        return false;
    }
    
    public Boolean doAutoRegister()
    {
        return false;
    }    
    
}
