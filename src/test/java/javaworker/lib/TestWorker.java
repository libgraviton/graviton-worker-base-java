package javaworker.lib;

import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

public class TestWorker extends WorkerAbstract {

    public boolean concerningRequestCalled = false;
    
    /**
     * worker logic is implemented here
     * 
     * @param body message body as object
     * 
     * @throws WorkerException
     */
    public void handleRequest(QueueEvent qevent) throws WorkerException {
        System.out.println("the testworker has been executed!");
        System.out.println("EVENT = " + qevent.getEvent());
        System.out.println("DOCUMENT = " + qevent.getDocument().get$ref());
        System.out.println("STATUS = " + qevent.getStatus().get$ref());
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
        return true;
    }
    
}
