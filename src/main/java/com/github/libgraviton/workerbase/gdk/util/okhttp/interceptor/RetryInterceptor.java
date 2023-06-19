package com.github.libgraviton.workerbase.gdk.util.okhttp.interceptor;

import com.github.libgraviton.workerbase.util.RetryRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class RetryInterceptor implements Interceptor {

  private static final Logger LOG = LoggerFactory.getLogger(RetryInterceptor.class);
  private final int retryCount;
  private final int waitInBetween;

  public RetryInterceptor() {
    this(
      6 * 10, // 10 minutes
      10
    );
  }

  public RetryInterceptor(int retryCount, int waitInBetween) {
    this.retryCount = retryCount;
    this.waitInBetween = waitInBetween;
  }

  @Override public @NotNull Response intercept(@NotNull Chain chain) throws IOException {

    final Request request = chain.request();
    CheckedSupplier<Response> responseSupplier = () -> {
      Response response = chain.proceed(request);

      if (List.of(500, 502, 503).contains(response.code())) {
        response.close();
        throw new IOException("Got response status " + response.code());
      }

      return response;
    };

    try {
      return RetryRegistry.retrySomething(
        retryCount,
        responseSupplier,
        Duration.ofSeconds(waitInBetween),
        (event) -> LOG.warn("Error on http request.", event.getLastThrowable())
      );
    } catch (Throwable e) {
      LOG.error(
        "Retries exhausted for http request",
        e
      );
      throw new IOException("Retries exhausted for http request", e);
    }
  }

}
