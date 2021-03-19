package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * stuff that is the same for all worker types
 */
abstract public class BaseWorker implements WorkerInterface {

  protected static final Logger LOG = LoggerFactory.getLogger(BaseWorker.class);

  protected Properties properties;

  protected String workerId;

  public Properties getProperties() {
    return properties;
  }

  public String getWorkerId() {
    return workerId;
  }

  /**
   * will be called after we're initialized, can contain some initial logic in the worker.
   *
   * @throws WorkerException when a problem occurs that prevents the Worker from working properly
   */
  public void onStartUp() throws WorkerException
  {
  }

  /**
   * initializes this worker, will be called by the library
   *
   * @param properties properties
   * @throws WorkerException when a problem occurs that prevents the Worker from working properly
   * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
   *
   */
  public void initialize(
      Properties properties) throws WorkerException, GravitonCommunicationException {

    this.properties = properties;
    workerId = properties.getProperty("graviton.workerId");

    try {
      Thread.setDefaultUncaughtExceptionHandler((t, e) -> LOG.error("Uncaught Exception", e));
    } catch (SecurityException e) {
      LOG.error("Could not set the Default Uncaught Exception Handler", e);
    }
  }

  /**
   * detects if an object is run from inside of a jar file.
   *
   * @param obj object to test
   * @return true if worker is run from a jar file else false
   */
  public static boolean isWorkerStartedFromJARFile(Object obj) {
    return WorkerUtil.isJarContext(obj);
  }

}
