package javaworker.lib;

import com.github.libgraviton.workerbase.FileWorkerAbstract;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.model.file.GravitonFile;
import com.github.libgraviton.workerbase.model.QueueEvent;

public class TestFileWorker extends FileWorkerAbstract {

    public boolean concerningRequestCalled = false;
    
    public GravitonFile fileObj;
    public boolean actionPresent;
    
    /**
     * worker logic is implemented here
     * 
     * @param queueEvent message body as object
     * 
     * @throws WorkerException
     */
    public void handleRequest(QueueEvent queueEvent) throws WorkerException {
        try {
            fileObj = getGravitonFile(queueEvent.getDocument().get$ref());
            actionPresent = isActionCommandPresent(this.fileObj, "doYourStuff");
            removeFileActionCommand(queueEvent.getDocument().get$ref(), "doYourStuff");
        } catch (GravitonCommunicationException e) {
            e.printStackTrace();
        }

    }
    
    /**
     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     * 
     * @param queueEvent message body as object
     * 
     * @return boolean true if not, false if yes
     */
    public boolean shouldHandleRequest(QueueEvent queueEvent) {
        concerningRequestCalled = true;
        return true;
    }
    
}
