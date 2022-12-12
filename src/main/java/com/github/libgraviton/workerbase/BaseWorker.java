package com.github.libgraviton.workerbase;

import java.util.Properties;

import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import io.activej.inject.annotation.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * stuff that is the same for all worker types
 */
abstract class BaseWorker implements WorkerInterface {

  protected static final Logger LOG = LoggerFactory.getLogger(BaseWorker.class);

  protected Properties properties;

  protected final WorkerScope workerScope;

  protected String workerId;

  public Properties getProperties() {
    return properties;
  }

  public String getWorkerId() {
    return workerId;
  }

  public BaseWorker(WorkerScope workerScope) {
    this.workerScope = workerScope;
  }

  public void initialize(Properties properties) throws WorkerException {
    this.properties = properties;
    workerId = properties.getProperty("graviton.workerId");

    try {
      Thread.setDefaultUncaughtExceptionHandler((t, e) -> LOG.error("Uncaught Exception", e));
    } catch (SecurityException e) {
      LOG.error("Could not set the Default Uncaught Exception Handler", e);
    }
  }
}
