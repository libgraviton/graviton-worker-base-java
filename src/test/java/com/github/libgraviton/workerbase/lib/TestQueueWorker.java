package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.QueueWorkerAbstract;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workerbase.util.CallbackRegistrar;
import io.activej.inject.annotation.Inject;

import java.util.concurrent.atomic.AtomicInteger;

@GravitonWorker
public class TestQueueWorker extends QueueWorkerAbstract {

    public boolean shouldHandleRequestCalled = false;

    public boolean hasStartedUp = false;

    public File fetchedFile;

    protected QueueEvent handledQueueEvent;

    public int callCount = 0;

    public int errorCount = 0;

    final public AtomicInteger afterCompleteCalled = new AtomicInteger(0);
    final public AtomicInteger afterStatusChangeCalled = new AtomicInteger(0);
    final public AtomicInteger afterExceptionCalled = new AtomicInteger(0);

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

    @Override
    public void addCallbacks(CallbackRegistrar callbackRegistrar) {
        callbackRegistrar.addStatusChangeCallback(
          (queueEventScope, status) -> afterStatusChangeCalled.incrementAndGet(),
          1
        );
        callbackRegistrar.addAfterCompleteCallback(
          workingDuration -> afterCompleteCalled.incrementAndGet(),
          1
        );
        callbackRegistrar.addExceptionCallback(
          (queueEventScope, t) -> afterExceptionCalled.incrementAndGet(),
          1
        );
    }

    public void onStartUp() {
        hasStartedUp = true;
    }
}
