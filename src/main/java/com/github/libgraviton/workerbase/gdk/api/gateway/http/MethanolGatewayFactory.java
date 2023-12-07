package com.github.libgraviton.workerbase.gdk.api.gateway.http;

import com.github.libgraviton.workerbase.gdk.util.http.interceptor.RetryInterceptor;
import com.github.mizosoft.methanol.Methanol;

import javax.net.ssl.*;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.Socket;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class MethanolGatewayFactory {

  private static final TrustManager MOCK_TRUST_MANAGER = new X509ExtendedTrustManager() {
    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

    }

    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
      return new java.security.cert.X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
      // empty method
    }
  };

  /**
   * gets the normal instance without retry interceptor
   *
   * @return instance
   */
  public static Methanol getInstance(boolean hasRetry, boolean trustAll, boolean hasCookie) throws NoSuchAlgorithmException, KeyManagementException {
    Methanol.Builder builder = Methanol.newBuilder();
    if (trustAll) {
      SSLContext sslContext = SSLContext.getInstance("SSL"); // OR TLS
      sslContext.init(null, new TrustManager[]{MOCK_TRUST_MANAGER}, new SecureRandom());
      builder.sslContext(sslContext);
    }

    if (hasRetry) {
      builder.interceptor(new RetryInterceptor());
    }

    if (hasCookie) {
      CookieManager cm = new CookieManager();
      cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
      CookieHandler.setDefault(cm);

      builder.cookieHandler(CookieHandler.getDefault());
    }

    builder.followRedirects(HttpClient.Redirect.ALWAYS);

    return builder.build();
  }
}
