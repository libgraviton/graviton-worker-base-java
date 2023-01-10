package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.WorkerScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * stuff that is the same for all worker types
 */
abstract class BaseWorker implements WorkerInterface {

  protected static final Logger LOG = LoggerFactory.getLogger(BaseWorker.class);

  protected final WorkerScope workerScope;

  public WorkerScope getWorkerScope() {
    return workerScope;
  }

  public BaseWorker(WorkerScope workerScope) {
    this.workerScope = workerScope;

    try {
      Thread.setDefaultUncaughtExceptionHandler((t, e) -> LOG.error("Uncaught Exception", e));
    } catch (SecurityException e) {
      LOG.error("Could not set the Default Uncaught Exception Handler", e);
    }
  }

  public String getWorkerId() {
    return getWorkerScope().getWorkerId();
  }
}
