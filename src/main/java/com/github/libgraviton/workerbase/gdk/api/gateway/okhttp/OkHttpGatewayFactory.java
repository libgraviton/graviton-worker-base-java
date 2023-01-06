package com.github.libgraviton.workerbase.gdk.api.gateway.okhttp;

import com.github.libgraviton.workerbase.gdk.util.okhttp.interceptor.RetryInterceptor;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

import javax.net.ssl.X509TrustManager;
import java.util.concurrent.TimeUnit;

public class OkHttpGatewayFactory {

  /**
   * gets the normal instance without retry interceptor
   *
   * @return instance
   */
  public static OkHttpClient getInstance(boolean hasRetry) {
    return getBaseBuilder(hasRetry).build();
  }

  /**
   * returns a client that trusts all certs
   *
   * @param hasRetry if should have retry or not
   * @return
   * @throws Exception
   */
  public static OkHttpClient getAllTrustingInstance(boolean hasRetry, OkHttpClient baseClient) throws Exception {
    Builder baseBuilder;
    if (baseClient != null) {
      baseBuilder = baseClient.newBuilder();
    } else {
      baseBuilder = getBaseBuilder(hasRetry);
    }

    baseBuilder
        .sslSocketFactory(WorkerUtil.getAllTrustingSocketFactory(), (X509TrustManager) WorkerUtil.getAllTrustingTrustManagers()[0])
        .hostnameVerifier((hostname, session) -> true);

    return baseBuilder.build();
  }

  private static Builder getBaseBuilder(boolean hasRetry) {
    Builder builder = new Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS);

    if (hasRetry) {
      builder.addInterceptor(new RetryInterceptor());
    }

    return builder;
  }
}
