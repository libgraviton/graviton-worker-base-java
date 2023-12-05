package com.github.libgraviton.workerbase.gdk.util.http.interceptor;

import com.github.libgraviton.workerbase.util.RetryRegistry;
import com.github.mizosoft.methanol.Methanol;
import io.github.resilience4j.core.functions.CheckedSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RetryInterceptor implements Methanol.Interceptor {

  private static final Logger LOG = LoggerFactory.getLogger(RetryInterceptor.class);
  private final int retryCount;
  private final int waitInBetween;

  public RetryInterceptor() {
    this(
      6 * 10, // 10 minutes
      5
    );
  }

  public RetryInterceptor(int retryCount, int waitInBetween) {
    this.retryCount = retryCount;
    this.waitInBetween = waitInBetween;
  }

  @Override
  public <T> HttpResponse<T> intercept(HttpRequest request, Chain<T> chain) throws IOException {
    CheckedSupplier<HttpResponse<T>> responseSupplier = () -> {
      HttpResponse<T> response = chain.forward(request);

      if (List.of(500, 502, 503).contains(response.statusCode())) {
        throw new IOException("Got response status " + response.statusCode());
      }

      return response;
    };

    try {
      return RetryRegistry.retrySomething(
        retryCount,
        responseSupplier,
        Duration.ofSeconds(waitInBetween),
        (event) -> LOG.warn("Error on http request: {}", event.getLastThrowable() == null ? "?" : event.getLastThrowable().getMessage())
      );
    } catch (Throwable e) {
      LOG.error(
        "Retries exhausted for http request",
        e
      );
      throw new IOException("Retries exhausted for http request", e);
    }
  }

  @Override
  public <T> CompletableFuture<HttpResponse<T>> interceptAsync(HttpRequest request, Chain<T> chain) {
    LOG.warn("Called async for retryInterceptor; is not supported.");
    return chain.forwardAsync(request);
  }
}
