package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workerbase.gdk.api.Response;
import io.github.resilience4j.core.EventConsumer;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.event.RetryOnRetryEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class RetryRegistry {

  private static io.github.resilience4j.retry.RetryRegistry registry;

  public static io.github.resilience4j.retry.RetryRegistry getInstance() {
    if (registry == null) {
      registry = io.github.resilience4j.retry.RetryRegistry.ofDefaults();
    }

    return registry;
  }

  public static <T> T retrySomethingForever(
    CheckedSupplier<T> supplier,
    EventConsumer<RetryOnRetryEvent> onRetry
  ) throws Throwable {
    return retrySomething(
      Integer.MAX_VALUE,
      supplier,
      Duration.of(2, ChronoUnit.SECONDS),
      onRetry
    );
  }

  public static <T> T retrySomething(
    Integer retryMax,
    CheckedSupplier<T> supplier,
    EventConsumer<RetryOnRetryEvent> onRetry
  ) throws Throwable {
    return retrySomething(
      retryMax,
      supplier,
      Duration.of(2, ChronoUnit.SECONDS),
      onRetry
    );
  }

  public static <T> T retrySomething(
    Integer retryMax,
    CheckedSupplier<T> supplier,
    Duration waitDuration,
    EventConsumer<RetryOnRetryEvent> onRetry
  ) throws Throwable {

    String retryName = UUID.randomUUID().toString();

    RetryConfig retryConfig = RetryConfig.custom()
      .maxAttempts(retryMax)
      .waitDuration(waitDuration)
      .build();

    getInstance().retry(retryName, retryConfig);

    try {
      getInstance().retry(retryName).getEventPublisher().onRetry(onRetry);

      CheckedSupplier<T> decoratedSupplier = Retry.decorateCheckedSupplier(
        getInstance().retry(retryName),
        supplier
      );

      return decoratedSupplier.get();
    } finally {
      getInstance().remove(retryName);
    }
  }

}
