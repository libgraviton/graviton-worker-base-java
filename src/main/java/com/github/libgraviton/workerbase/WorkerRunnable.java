package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.workerbase.exception.WorkerExceptionRetriable;
import com.github.libgraviton.workerbase.gdk.requestexecutor.exception.AuthenticatorException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workerbase.util.RetryRegistry;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WorkerRunnable implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerRunnable.class);

    @FunctionalInterface
    public interface GetWorkloadCallback {
        WorkerRunnableInterface getWorkload(QueueEventScope queueEventScope) throws Throwable;
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
        boolean isRevelant(QueueEventScope queueEventScope) throws Throwable;
    }

    private final QueueEvent queueEvent;
    private final GetWorkloadCallback getWorkloadCallback;
    public final AfterStatusChangeCallback afterStatusChangeCallback;
    private final AfterExceptionCallback afterExceptionCallback;
    private final RelevantEventCallback relevantEventCallback;
    private final List<AfterCompleteCallback> afterCompleteCallbacks = new ArrayList<>();

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
        afterCompleteCallbacks.add(afterCompleteCallback);
        this.afterExceptionCallback = afterExceptionCallback;
        this.relevantEventCallback = relevantEventCallback;
    }

    public void addAfterCompleteCallback(AfterCompleteCallback afterCompleteCallback) {
        afterCompleteCallbacks.add(afterCompleteCallback);
    }

    @Override
    public void run() {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final int retryLimit = Integer.parseInt(WorkerProperties.DIRECT_RETRY_LIMIT.get());

        try {
            final QueueEventScope queueEventScope = DependencyInjection.getInstance(QueueEventScope.class);
            queueEventScope.setQueueEvent(queueEvent);

            // is it relevant?
            if (!relevantEventCallback.isRevelant(queueEventScope)) {
                afterStatusChangeCallback.onStatusChange(EventStatusStatus.Status.IGNORED);
                return;
            }

            afterStatusChangeCallback.onStatusChange(EventStatusStatus.Status.WORKING);

            RetryRegistry.retrySomething(
              retryLimit,
              () -> {
                  getWorkloadCallback.getWorkload(queueEventScope).doWork(queueEventScope);
                  return null;
              },
              Duration.ofSeconds(2),
              (event) -> LOG.warn(
                "Worker returned a retriable Exception - will directly try again (try {} of {})",
                event.getNumberOfRetryAttempts(),
                retryLimit
              ),
              (throwable) -> {
                  int hans = 3;
                  return throwable instanceof WorkerExceptionRetriable;
              }, // only retry this type!
              null
            );

            afterStatusChangeCallback.onStatusChange(EventStatusStatus.Status.DONE);
        } catch (Throwable t) {
            afterExceptionCallback.onException(t);
        } finally {
            // call all after completes!
            afterCompleteCallbacks.forEach(s -> s.onComplete(stopwatch.elapsed()));
        }
    }
}
