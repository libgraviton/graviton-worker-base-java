package ch.swisscom.graviton.javaworker;

import ch.swisscom.graviton.javaworker.lib.WorkerAbstract;
import ch.swisscom.graviton.javaworker.lib.WorkerException;
import ch.swisscom.graviton.javaworker.lib.model.QueueEvent;

public class ExampleWorker extends WorkerAbstract {

    /**
     * worker logic is implemented here
     * 
     * @param body message body as object
     * 
     * @throws WorkerException
     */
    public void handleRequest(QueueEvent qevent) throws WorkerException {
        System.out.println("JUHUUUUUUUUUUUUUUUU");
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
        return true;
    }
    
}
