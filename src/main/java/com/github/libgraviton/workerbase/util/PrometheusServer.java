package com.github.libgraviton.workerbase.util;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.BufferPoolsExports;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryAllocationExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;
import io.prometheus.client.hotspot.VersionInfoExports;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusServer {

  private static final Logger LOG = LoggerFactory.getLogger(PrometheusServer.class);

  public PrometheusServer(Properties properties) {
    init(properties.getProperty("prom.jvm", "false").equals("true"));
  }

  private void init(boolean loadJvmMetrics) {
    try {
      HTTPServer server = new HTTPServer(9999);
      LOG.info("Started prometheus HTTPServer for metrics on http://0.0.0.0:9999/metrics");
    } catch (Throwable t) {
      LOG.error("Could not start prometheus metrics HTTPServer", t);
    }

    // load jvm metrics?
    if (!loadJvmMetrics) {
      return;
    }

    new StandardExports().register(CollectorRegistry.defaultRegistry);
    new MemoryPoolsExports().register(CollectorRegistry.defaultRegistry);
    new MemoryAllocationExports().register(CollectorRegistry.defaultRegistry);
    new BufferPoolsExports().register(CollectorRegistry.defaultRegistry);
    new GarbageCollectorExports().register(CollectorRegistry.defaultRegistry);
    new ThreadExports().register(CollectorRegistry.defaultRegistry);
    new ClassLoadingExports().register(CollectorRegistry.defaultRegistry);
    new VersionInfoExports().register(CollectorRegistry.defaultRegistry);
  }
}
