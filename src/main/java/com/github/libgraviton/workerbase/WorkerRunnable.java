package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.workerbase.exception.WorkerExceptionRetriable;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workerbase.util.RetryRegistry;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;

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
    void onException(QueueEventScope queueEventScope, Throwable t);
  }

  @FunctionalInterface
  public interface AfterStatusChangeCallback {
    void onStatusChange(QueueEventScope queueEventScope, EventStatusStatus.Status status) throws Throwable;
  }

  @FunctionalInterface
  public interface RelevantEventCallback {
    boolean isRevelant(QueueEventScope queueEventScope) throws Throwable;
  }

  private final QueueEvent queueEvent;
  private final GetWorkloadCallback getWorkloadCallback;
  public final Collection<AfterStatusChangeCallback> afterStatusChangeCallbacks;
  private final Collection<AfterExceptionCallback> afterExceptionCallbacks;
  private final RelevantEventCallback relevantEventCallback;
  private final Collection<AfterCompleteCallback> afterCompleteCallbacks;

  public WorkerRunnable(
    final QueueEvent queueEvent,
    final GetWorkloadCallback getWorkloadCallback,
    final Collection<AfterStatusChangeCallback> afterStatusChangeCallbacks,
    final Collection<AfterCompleteCallback> afterCompleteCallbacks,
    final Collection<AfterExceptionCallback> afterExceptionCallbacks,
    final RelevantEventCallback relevantEventCallback
  ) {
    this.queueEvent = queueEvent;
    this.getWorkloadCallback = getWorkloadCallback;
    this.afterStatusChangeCallbacks = afterStatusChangeCallbacks;
    this.afterCompleteCallbacks = afterCompleteCallbacks;
    this.afterExceptionCallbacks = afterExceptionCallbacks;
    this.relevantEventCallback = relevantEventCallback;
  }

  @Override
  public void run() {
    final Stopwatch stopwatch = Stopwatch.createStarted();
    final int retryLimit = Integer.parseInt(WorkerProperties.DIRECT_RETRY_LIMIT.get());

    final QueueEventScope queueEventScope = DependencyInjection.getInstance(QueueEventScope.class);
    queueEventScope.setQueueEvent(queueEvent);

    try {

      // is it relevant?
      if (!relevantEventCallback.isRevelant(queueEventScope)) {
        onStatusChange(queueEventScope, EventStatusStatus.Status.IGNORED);
        return;
      }

      onStatusChange(queueEventScope, EventStatusStatus.Status.WORKING);

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
        (throwable) -> throwable instanceof WorkerExceptionRetriable, // only retry this type!
        null
      );

      onStatusChange(queueEventScope, EventStatusStatus.Status.DONE);
    } catch (Throwable t) {
      LOG.debug(
        "Exception '{}' thrown, starting onException with '{}' callbacks.",
        t.getClass().getName(),
        afterExceptionCallbacks.size()
      );
      afterExceptionCallbacks.forEach(callback -> {
        LOG.debug("Calling onException callback '{}'", callback);
        callback.onException(queueEventScope, t);
      });
    } finally {
      // call all after completes!
      final Duration elapsed = stopwatch.elapsed();
      LOG.debug(
        "run() of worker finished, elapsed '{}ms'. Starting onComplete with '{}' callbacks.",
        elapsed.toMillis(),
        afterCompleteCallbacks.size()
      );
      afterCompleteCallbacks.forEach(s -> {
        LOG.debug("Calling onComplete callback '{}'", s);
        s.onComplete(elapsed);
      });
    }
  }

  private void onStatusChange(QueueEventScope scope, EventStatusStatus.Status status) throws Throwable {
    LOG.debug(
      "onStatusChange() to status '{}'. Starting onStatusChange with '{}' callbacks.",
      status.value(),
      afterStatusChangeCallbacks.size()
    );
    for (AfterStatusChangeCallback callback : afterStatusChangeCallbacks) {
      LOG.debug("Calling onStatusChange callback '{}'", callback);
      callback.onStatusChange(scope, status);
    }
  }
}
