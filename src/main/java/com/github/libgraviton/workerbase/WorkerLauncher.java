package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.exception.WorkerExceptionFatal;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.util.PrometheusServer;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Worker class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @version $Id: $Id
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 */
@GravitonWorkerDiScan
public final class WorkerLauncher {

  private static final Logger LOG = LoggerFactory.getLogger(WorkerLauncher.class);

  private final WorkerInterface worker;
  private final PrometheusServer prometheusServer;
  private final QueueWorkerRunner queueWorkerRunner;

  public WorkerLauncher(
    WorkerInterface worker,
    Properties properties,
    String applicationName
  ) {

    this.worker = worker;

    LOG.info(
      "Starting '{} {}' (class '{}', worker-base '{}'). Runtime '{}' version '{}', TZ '{}'",
      applicationName,
      properties.getProperty("application.version"),
      worker.getClass().getName(),
      WorkerUtil.getWorkerBaseVersion(),
      System.getProperty("java.runtime.name"),
      System.getProperty("java.runtime.version"),
      System.getProperty("user.timezone")
    );

    final TimerTask memoryReporter = new TimerTask() {

      private final AtomicInteger lastRecordedUsage = new AtomicInteger(0);
      private final AtomicInteger maxRecordedAllocation = new AtomicInteger(0);

      @Override
      public void run() {
        final int mb = 1048576;
        long totalMemory = Runtime.getRuntime().totalMemory() / mb;
        long freeMemory = Runtime.getRuntime().freeMemory() / mb;
        long maxMemory = Runtime.getRuntime().maxMemory() / mb;
        long usedMemory = totalMemory - freeMemory;

        int percentageUsed = (int) (usedMemory * 100.0 / maxMemory + 0.5);
        int percentageAllocated = (int) (totalMemory * 100.0 / maxMemory + 0.5);

        if (percentageUsed == lastRecordedUsage.get()) {
          return;
        }

        if (percentageAllocated > maxRecordedAllocation.get()) {
          maxRecordedAllocation.set(percentageAllocated);
        }

        LOG.info(
          "Heap memory stats: Using {} MB of max {} MB ({}%). Allocated {} MB ({}%, max {}%).",
          (int) Math.floor(usedMemory),
          maxMemory,
          percentageUsed,
          (int) Math.floor(totalMemory),
          percentageAllocated,
          maxRecordedAllocation.get()
        );

        lastRecordedUsage.set(percentageUsed);
      }
    };

    final Timer timer = new Timer();
    // every 60 seconds
    timer.scheduleAtFixedRate(memoryReporter, 1000, 60000);

    prometheusServer = new PrometheusServer(worker.getWorkerId());

    if (worker instanceof QueueWorkerAbstract) {
      queueWorkerRunner = new QueueWorkerRunner((QueueWorkerAbstract) worker);
    } else {
      queueWorkerRunner = null;
    }
  }

  public WorkerInterface getWorker() {
    return worker;
  }

  public QueueWorkerRunner getQueueWorkerRunner() {
    return queueWorkerRunner;
  }

  /**
   * setup worker
   *
   * @throws WorkerException If connecting to queue is impossible
   */
  public void run() throws WorkerException {
    if (worker == null) {
      throw new WorkerExceptionFatal("No worker set to be run...");
    }

    // on startup call
    worker.onStartUp();

    // wire up healthcheck
    try {
      setupHealthcheck();
    } catch (Throwable t) {
      LOG.error("Unable to setup healthcheck server", t);
    }

    if (worker instanceof StandaloneWorker) {
      ((StandaloneWorker) this.worker).run();
    } else if (worker instanceof QueueWorkerAbstract && queueWorkerRunner != null) {
      queueWorkerRunner.run();
    } else {
      throw new WorkerExceptionFatal("The worker is not runnable!");
    }
  }

  private void setupHealthcheck() throws IOException {
    int port = 9000;
    HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

    // Create a context for your lambda function
    server.createContext("/", httpExchange -> {
      boolean isOk;
      try {
        isOk = worker.doHealthCheck();
      } catch (Throwable t) {
        LOG.warn("Healthcheck failed", t);
        isOk = false;
      }

      String result = isOk ? "OK" : "FAILED";
      httpExchange.sendResponseHeaders(200, result.length());

      OutputStream os = httpExchange.getResponseBody();
      os.write(result.getBytes());
      os.close();
    });

    server.start();
    LOG.info("Started healthcheck server on port '{}'", port);
  }

  public void stop() {
    prometheusServer.stop();
    if (queueWorkerRunner != null) {
      queueWorkerRunner.close();
    }
  }
}
