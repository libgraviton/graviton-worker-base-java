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

    public void handleRequest(QueueEventScope queueEventScope) throws WorkerException {
        handledQueueEvent = queueEventScope.getQueueEvent();

        try {
            fetchedFile = queueEventScope.getFileEndpoint().getFileMetadata("test-workerfile");
        } catch (Throwable t) {
            throw new WorkerException("Error fetching file", t);
        }

        callCount++;

        // here we should fail! -> fail max 3 times!
        if (handledQueueEvent.getCoreUserId() != null && handledQueueEvent.getCoreUserId().equals("PLEASE_FAIL_HERE!") && errorCount < 3) {
            errorCount++;
            throw new WorkerException("YES_I_DO_FAIL_NOW");
        }
    }

    /**

     * Here, the worker should decide if this requests concerns him in the first
     * place. If false is returned, we ignore the message..
     *
     * @return boolean true if not, false if yes
     */
    public boolean shouldHandleRequest(QueueEventScope queueEventScope) {
        this.shouldHandleRequestCalled = true;
        if (queueEventScope.getQueueEvent().getCoreUserId() == null) {
            return true;
        }

        return !queueEventScope.getQueueEvent().getCoreUserId().equals("SPECIAL_USER");
    }

    public void onStartUp() {
        hasStartedUp = true;
    }
}
