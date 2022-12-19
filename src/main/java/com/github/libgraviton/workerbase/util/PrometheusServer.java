package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.sun.net.httpserver.HttpServer;
import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusServer {

  private static final Logger LOG = LoggerFactory.getLogger(PrometheusServer.class);

  private static boolean started = false;

  private Thread serverThread;
  private HttpServer server;

  public PrometheusServer(String appName) {
    if (!started) {
      init(appName);
    }
  }

  private void init(String appName) {
    final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    // app name
    registry.config().commonTags("application", appName);
    final int prometheusPort = Integer.parseInt(WorkerProperties.PROMETHEUS_PORT.get());

    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(prometheusPort), 0);
      server.createContext("/metrics", httpExchange -> {
        String response = registry.scrape();
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = httpExchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      });
      server.setExecutor(null);
      server.start();

      LOG.info("Started prometheus HTTPServer for metrics on http://0.0.0.0:{}/metrics", prometheusPort);
    } catch (Throwable t) {
      LOG.error("Could not start prometheus metrics HTTPServer", t);
    }

    started = true;

    // load jvm metrics?
    new ProcessMemoryMetrics().bindTo(registry);
    new ProcessThreadMetrics().bindTo(registry);
    new ClassLoaderMetrics().bindTo(registry);
    new JvmMemoryMetrics().bindTo(registry);
    new JvmThreadMetrics().bindTo(registry);
    try (JvmHeapPressureMetrics metric = new JvmHeapPressureMetrics()) {
      metric.bindTo(registry);
    }
    try (JvmGcMetrics metric = new JvmGcMetrics()) {
      metric.bindTo(registry);
    }
    new JvmInfoMetrics().bindTo(registry);
    new ProcessorMetrics().bindTo(registry);
    new FileDescriptorMetrics().bindTo(registry);
    new UptimeMetrics().bindTo(registry);

    // add to global
    Metrics.addRegistry(registry);
  }

  public void stop() {
    if (server != null) {
      server.stop(9);
    }
  }
}
