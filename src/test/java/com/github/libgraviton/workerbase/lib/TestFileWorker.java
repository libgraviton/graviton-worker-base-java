package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.FileWorkerAbstract;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.model.QueueEvent;

import java.util.Arrays;
import java.util.List;

public class TestFileWorker extends FileWorkerAbstract {

    public boolean concerningRequestCalled = false;
    public boolean shouldHandleRequestMocked = true;

    public File fileObj;
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
            actionPresent = isActionCommandPresent(this.fileObj, getActionsOfInterest(queueEvent).get(0));
            removeFileActionCommand(queueEvent.getDocument().get$ref(), getActionsOfInterest(queueEvent).get(0));
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
    public boolean shouldHandleRequest(QueueEvent queueEvent) throws WorkerException, GravitonCommunicationException {
        if (shouldHandleRequestMocked) {
            concerningRequestCalled = true;
            return true;
        }
        return super.shouldHandleRequest(queueEvent);
    }

    @Override
    public List<String> getActionsOfInterest(QueueEvent queueEvent) {
        return Arrays.asList("doYourStuff");
    }

    protected GravitonApi initGravitonApi() {
        return null;
    }
}
