package com.github.libgraviton.workerbase.util;

import com.sun.net.httpserver.HttpServer;
import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusServer {

  private static final Logger LOG = LoggerFactory.getLogger(PrometheusServer.class);

  private static boolean started = false;

  public PrometheusServer(String appName, Properties properties) {
    if (!started) {
      init(appName, properties.getProperty("prom.jvm", "false").equals("true"));
    }
  }

  private void init(String appName, boolean loadJvmMetrics) {

    final PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    // app name
    registry.config().commonTags("application", appName);

    try {
      HttpServer server = HttpServer.create(new InetSocketAddress(9999), 0);
      server.createContext("/metrics", httpExchange -> {
        String response = registry.scrape();
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = httpExchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      });

      new Thread(server::start).start();

      LOG.info("Started prometheus HTTPServer for metrics on http://0.0.0.0:9999/metrics");
    } catch (Throwable t) {
      LOG.error("Could not start prometheus metrics HTTPServer", t);
    }

    started = true;

    // load jvm metrics?
    if (loadJvmMetrics) {
      new ProcessMemoryMetrics().bindTo(registry);
      new ProcessThreadMetrics().bindTo(registry);
      new ClassLoaderMetrics().bindTo(registry);
      new JvmMemoryMetrics().bindTo(registry);
      new JvmGcMetrics().bindTo(registry);
      new ProcessorMetrics().bindTo(registry);
      new JvmThreadMetrics().bindTo(registry);
    }

    // add to global
    Metrics.addRegistry(registry);
  }
}
