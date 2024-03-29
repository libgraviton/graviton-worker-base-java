package com.github.libgraviton.workerbase.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * this is a singleton implementation/wrapper for properties handling
 * inside a worker. this was added as i found it frustrating that
 * 1) we pass around 'properties' mostly - but not always. still at
 * some points, static calls where made to PropertiesLoader. no clear concept.
 * 2) in tests; it is then very hard to set test specific properties
 * 3) accept both instance and static use - and still have the same properties
 */
public class WorkerProperties {

  private static final String CACHE_ENV_PATH = "PROPERTIES_CACHE_FILE";

  public record WorkerProperty(String name) {
    @Override
    public String toString() {
      return name;
    }

    public String get() {
      return WorkerProperties.getProperty(name);
    }
  }

  public static final WorkerProperty GRAVITON_BASE_URL = new WorkerProperty("graviton.base.url");
  public static final WorkerProperty GATEWAY_BASE_URL = new WorkerProperty("gateway.url");
  public static final WorkerProperty GATEWAY_USERNAME = new WorkerProperty("gateway.username");
  public static final WorkerProperty GATEWAY_PASSWORD = new WorkerProperty("gateway.password");
  public static final WorkerProperty QUEUE_HOST = new WorkerProperty("queue.host");
  public static final WorkerProperty QUEUE_PORT = new WorkerProperty("queue.port");
  public static final WorkerProperty QUEUE_MGMTPORT = new WorkerProperty("queue.mgmtPort");
  public static final WorkerProperty QUEUE_USER = new WorkerProperty("queue.user");
  public static final WorkerProperty QUEUE_PASSWORD = new WorkerProperty("queue.password");
  public static final WorkerProperty QUEUE_VIRTUALHOST = new WorkerProperty("queue.virtualhost");
  public static final WorkerProperty GATEWAY_JWT_LIFETIME = new WorkerProperty("gateway.jwt_lifetime");
  public static final WorkerProperty GRAVITON_SUBSCRIPTION = new WorkerProperty("graviton.subscription");
  public static final WorkerProperty WORKER_ID = new WorkerProperty("graviton.workerId");
  public static final WorkerProperty AUTH_PREFIX_USERNAME = new WorkerProperty("graviton.authentication.prefix.username");
  public static final WorkerProperty AUTH_HEADER_NAME = new WorkerProperty("graviton.authentication.header.name");
  public static final WorkerProperty PROMETHEUS_PORT = new WorkerProperty("graviton.prometheus.port");
  public static final WorkerProperty THREADPOOL_SIZE = new WorkerProperty("executor.threadpool.size");
  public static final WorkerProperty WORKER_MAIN_CLASS = new WorkerProperty("worker.mainClass");
  public static final WorkerProperty HTTP_CLIENT_DORETRY = new WorkerProperty("graviton.okhttp.shouldRetry");
  public static final WorkerProperty HTTP_CLIENT_FORCE_HTTP1_1 = new WorkerProperty("graviton.okhttp.forcehttp11");
  public static final WorkerProperty HTTP_CLIENT_TLS_TRUST_ALL = new WorkerProperty("graviton.okhttp.trustAll");
  public static final WorkerProperty STATUSHANDLER_RETRY_LIMIT = new WorkerProperty("graviton.statushandler.retrylimit");
  public static final WorkerProperty DI_CLASS_SCAN_USE_CACHE = new WorkerProperty("graviton.di.class_scan.use_cache");
  public static final WorkerProperty DIRECT_RETRY_LIMIT = new WorkerProperty("graviton.direct_retry.limit");

  private static final Logger LOG = LoggerFactory.getLogger(WorkerProperties.class);

  private static Properties loadedProperties = new Properties();
  private static boolean alreadyLoaded = false;

  private static final HashMap<String, String> propertyOverrides = new HashMap<>();

  private static class InnerProperties extends Properties {
    @Override
    public String getProperty(String name) {
      return WorkerProperties.getProperty(name);
    }

    @Override
    public Object get(Object key) {
      return WorkerProperties.getProperty(String.valueOf(key));
    }

    @Override
    public Object setProperty(String key, String value) {
      return WorkerProperties.setProperty(key, value);
    }

    @Override
    public Enumeration<?> propertyNames() {
      return Collections.enumeration(stringPropertyNames());
    }

    @Override
    public Set<String> stringPropertyNames() {
      return WorkerProperties.stringPropertyNames();
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
      return super.getOrDefault(key, defaultValue);
    }
  }

  public static Properties load() throws IOException {
    if (!alreadyLoaded) {
      // is there a cached one?
      String cachedProps = System.getenv(CACHE_ENV_PATH);
      if (cachedProps != null && new File(cachedProps).exists()) {
        try (InputStream inputStream = new GZIPInputStream(new FileInputStream(cachedProps))) {
          loadedProperties = new Properties();
          loadedProperties.load(inputStream);
        }
      } else {
        loadedProperties = PropertiesLoader.load();
      }
      alreadyLoaded = true;
    }
    return new InnerProperties();
  }

  public static void persist() throws IOException {
    String cacheFile = System.getenv(CACHE_ENV_PATH);
    if (cacheFile == null) {
      return;
    }

    try (OutputStream outputStream = new GZIPOutputStream(new FileOutputStream(cacheFile))) {
      loadedProperties.store(outputStream, "Serialized Properties");
    }
  }

  public static void addOverrides(Map<String, String> map) {
    propertyOverrides.putAll(map);
  }

  public static void setOverride(String name, String value) {
    propertyOverrides.put(name, value);
  }

  public static void clearOverride(String name) {
    propertyOverrides.remove(name);
  }

  public static void clearOverrides() {
    propertyOverrides.clear();
  }

  public static String getProperty(String name, String defaultValue) {
    String value = getProperty(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  public static String getProperty(String name) {
    if (!alreadyLoaded) {
      LOG.warn("Properties were called here without explicit call to WorkerProperties.load() before! "
        .concat("This is discouraged as the error handling is disabled here. Please fix that. Continuing ignoring errors.")
      );

      try {
        WorkerProperties.load();
      } catch (IOException e) {
        LOG.error("Error on explicit loading properties", e);
      }
    }

    return propertyOverrides.getOrDefault(
      name,
      loadedProperties.getProperty(name)
    );
  }

  public static Object setProperty(String name, String value) {
    getProperty("");
    return loadedProperties.setProperty(name, value);
  }

  public static Set<String> stringPropertyNames() {
    Set<String> names = new HashSet<>();
    getProperty("");
    names.addAll(loadedProperties.stringPropertyNames());
    names.addAll(propertyOverrides.keySet());
    return names;
  }
}
