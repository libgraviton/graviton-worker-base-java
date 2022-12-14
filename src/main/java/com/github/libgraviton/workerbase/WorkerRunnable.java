package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.google.common.base.Stopwatch;

import java.time.Duration;

public class WorkerRunnable implements Runnable {

    @FunctionalInterface
    public interface GetWorkloadCallback {
        WorkerRunnableInterface getWorkload() throws Throwable;
    }

    @FunctionalInterface
    public interface AfterCompleteCallback {
        void onComplete(Duration workingDuration);
    }

    @FunctionalInterface
    public interface AfterExceptionCallback {
        void onException(Throwable t);
    }

    @FunctionalInterface
    public interface AfterStatusChangeCallback {
        void onStatusChange(EventStatusStatus.Status status) throws Throwable;
    }

    @FunctionalInterface
    public interface RelevantEventCallback {
        boolean isRevelant(QueueEvent body) throws Throwable;
    }

    private final QueueEvent queueEvent;
    private final GetWorkloadCallback getWorkloadCallback;
    public final AfterStatusChangeCallback afterStatusChangeCallback;
    private final AfterCompleteCallback afterCompleteCallback;
    private final AfterExceptionCallback afterExceptionCallback;
    private final RelevantEventCallback relevantEventCallback;

    public WorkerRunnable(
            final QueueEvent queueEvent,
            final GetWorkloadCallback getWorkloadCallback,
            final AfterStatusChangeCallback afterStatusChangeCallback,
            final AfterCompleteCallback afterCompleteCallback,
            final AfterExceptionCallback afterExceptionCallback,
            final RelevantEventCallback relevantEventCallback
    ) {
        this.queueEvent = queueEvent;
        this.getWorkloadCallback = getWorkloadCallback;
        this.afterStatusChangeCallback = afterStatusChangeCallback;
        this.afterCompleteCallback = afterCompleteCallback;
        this.afterExceptionCallback = afterExceptionCallback;
        this.relevantEventCallback = relevantEventCallback;
    }

    @Override
    public void run() {
        Stopwatch stopwatch = Stopwatch.createStarted();

        try {
            // is it relevant?
            if (!relevantEventCallback.isRevelant(queueEvent)) {
                afterStatusChangeCallback.onStatusChange(EventStatusStatus.Status.IGNORED);
                return;
            }

            afterStatusChangeCallback.onStatusChange(EventStatusStatus.Status.WORKING);

            QueueEventScope queueEventScope = DependencyInjection.getInstance(QueueEventScope.class);
            queueEventScope.setQueueEvent(queueEvent);

            // call the worker
            getWorkloadCallback.getWorkload().doWork(queueEvent, queueEventScope);

            afterStatusChangeCallback.onStatusChange(EventStatusStatus.Status.DONE);
        } catch (Throwable t) {
            afterExceptionCallback.onException(t);
        } finally {
            afterCompleteCallback.onComplete(stopwatch.elapsed());
        }
    }
}
