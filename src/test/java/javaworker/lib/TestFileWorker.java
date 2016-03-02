package javaworker.lib;

import com.github.libgraviton.workerbase.FileWorkerAbstract;
import com.github.libgraviton.workerbase.WorkerException;
import com.github.libgraviton.workerbase.model.GravitonFile;
import com.github.libgraviton.workerbase.model.QueueEvent;

public class TestFileWorker extends FileWorkerAbstract {

    public boolean concerningRequestCalled = false;
    
    public GravitonFile fileObj;
    public boolean actionPresent;
    
    /**
     * worker logic is implemented here
     * 
     * @param body message body as object
     * 
     * @throws WorkerException
     */
    public void handleRequest(QueueEvent qevent) throws Exception {
        this.fileObj = this.getGravitonFile(qevent.getDocument().get$ref());        
        this.actionPresent = this.isActionCommandPresent(this.fileObj, "doYourStuff");
        this.removeFileActionCommand(qevent.getDocument().get$ref(), "doYourStuff");
    }
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     * 
     * @param body message body as object
     * 
     * @return boolean true if not, false if yes
     */
    public boolean shouldHandleRequest(QueueEvent qevent) {
        this.concerningRequestCalled = true;
        return true;
    }
    
}
