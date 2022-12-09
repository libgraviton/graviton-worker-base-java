package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.FileQueueWorkerAbstract;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.model.QueueEvent;

import java.util.List;

public class TestFileQueueWorker extends FileQueueWorkerAbstract {

    public boolean concerningRequestCalled = false;
    public boolean shouldHandleRequestMocked = true;

    public File fileObj;
    public boolean actionPresent;

    /**
     * worker logic is implemented here
     * 
     * @param queueEvent message body as object

     */
    public void handleFileRequest(QueueEvent queueEvent, File file, QueueEventScope queueEventScope) {
        fileObj = file;
        actionPresent = isActionCommandPresent(this.fileObj, getActionsOfInterest(queueEvent).get(0));
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
        return List.of("doYourStuff");
    }

    @Override
    public void onStartUp() throws WorkerException {

    }
}
