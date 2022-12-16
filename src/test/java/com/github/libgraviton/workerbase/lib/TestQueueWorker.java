package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.QueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.model.QueueEvent;
import io.activej.inject.annotation.Inject;

@GravitonWorker
public class TestQueueWorker extends QueueWorkerAbstract {

    public boolean shouldHandleRequestCalled = false;

    public boolean hasStartedUp = false;

    public File fetchedFile;

    protected QueueEvent handledQueueEvent;

    public int callCount = 0;

    public int errorCount = 0;

    @Inject
    public TestQueueWorker(WorkerScope workerScope) {
        super(workerScope);
    }

    /**
     * worker logic is implemented here
     * 
     * @param qevent queue event from request
     *
     */
    public void handleRequest(QueueEvent qevent, QueueEventScope queueEventScope) throws WorkerException {
        handledQueueEvent = qevent;

        try {
            fetchedFile = queueEventScope.getFileEndpoint().getFileMetadata("test-workerfile");
        } catch (Throwable t) {
            throw new WorkerException("Error fetching file", t);
        }

        callCount++;

        // here we should fail! -> fail max 3 times!
        if (qevent.getCoreUserId() != null && qevent.getCoreUserId().equals("PLEASE_FAIL_HERE!") && errorCount < 3) {
            errorCount++;
            throw new WorkerException("YES_I_DO_FAIL_NOW");
        }
    }

    /**

     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     * 
     * @param qevent queue event from request
     * 
     * @return boolean true if not, false if yes
     */
    public boolean shouldHandleRequest(QueueEvent qevent, QueueEventScope queueEventScope) {
        this.shouldHandleRequestCalled = true;
        if (qevent.getCoreUserId() == null) {
            return true;
        }

        return !qevent.getCoreUserId().equals("SPECIAL_USER");
    }

    public void onStartUp() {
        hasStartedUp = true;
    }
}
