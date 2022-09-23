package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.workerbase.model.QueueEvent;;

public class WorkerRunnable implements Runnable {

    @FunctionalInterface
    public interface AfterCompleteCallback {
        void onComplete();
    }

    @FunctionalInterface
    public interface AfterExceptionCallback {
        void onException(Throwable t);
    }

    @FunctionalInterface
    public interface AfterStatusChangeCallback {
        void onStatusChange(EventStatusStatus.Status status) throws Throwable;
    }

    private final QueueEvent queueEvent;
    private final WorkerRunnableInterface workload;
    public final AfterStatusChangeCallback afterStatusChangeCallback;
    private final AfterCompleteCallback afterCompleteCallback;
    private final AfterExceptionCallback afterExceptionCallback;

    public WorkerRunnable(
            QueueEvent queueEvent,
            WorkerRunnableInterface workload,
            AfterStatusChangeCallback afterStatusChangeCallback,
            AfterCompleteCallback afterCompleteCallback,
            AfterExceptionCallback afterExceptionCallback
    ) {
        this.queueEvent = queueEvent;
        this.workload = workload;
        this.afterStatusChangeCallback = afterStatusChangeCallback;
        this.afterCompleteCallback = afterCompleteCallback;
        this.afterExceptionCallback = afterExceptionCallback;
    }

    @Override
    public void run() {
        try {
            afterStatusChangeCallback.onStatusChange(EventStatusStatus.Status.WORKING);

            // call the worker
            workload.doWork(queueEvent);

            afterStatusChangeCallback.onStatusChange(EventStatusStatus.Status.DONE);
        } catch (Throwable t) {
            afterExceptionCallback.onException(t);
        } finally {
            afterCompleteCallback.onComplete();
        }
    }
}
