package com.github.libgraviton.workerbase.util;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public class DownloadClient {

  /**
   * TrustManager that trusts all certs
   */
  private static final TrustManager[] trustAllCerts = new TrustManager[]{
      new X509TrustManager() {
        @Override
        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
            throws CertificateException {
        }

        @Override
        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
            throws CertificateException {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return new java.security.cert.X509Certificate[]{};
        }
      }
  };

  /**
   * SSL Context
   */
  private static final SSLContext trustAllSslContext;

  static {
    try {
      trustAllSslContext = SSLContext.getInstance("SSL");
      trustAllSslContext.init(null, trustAllCerts, new java.security.SecureRandom());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * SSL Socket Factory
   */
  private static final SSLSocketFactory trustAllSslSocketFactory = trustAllSslContext
      .getSocketFactory();

  public static OkHttpClient getDownloadClient(Boolean acceptAllSslCerts) {
    OkHttpClient okHttpClient = new OkHttpClient();
    Builder builder = okHttpClient.newBuilder()
        .retryOnConnectionFailure(true);

    if (acceptAllSslCerts) {
      builder
          .sslSocketFactory(trustAllSslSocketFactory, (X509TrustManager) trustAllCerts[0])
          .hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
              return true;
            }
          });
    }

    return builder.build();
  }

  /**
   * Downloads a file and returns its content as string
   *
   * @param url the url to get
   * @param acceptAllSslCerts if we should accept all certificates
   *
   * @return response body
   * @throws Exception
   */
  public static String downloadFile(String url, Boolean acceptAllSslCerts) throws Exception {
    Request request = new Request.Builder().url(url).build();

    // execute it..
    Response response = getDownloadClient(acceptAllSslCerts).newCall(request).execute();

    try (ResponseBody body = response.body()) {
      if (response.code() < 200 || response.code() > 300) {
        throw new Exception(
            "Download returned an unexpected status code of " + response.code());
      }
      return body.string();
    } catch (Exception e) {
      throw new Exception("Error downloading URL '" + url + "'", e);
    } finally {
      if (response.body() != null) {
        response.body().close();
      }
    }
  }

  /**
   * Downloads a file to disk
   *
   * @param url url to get
   * @param targetPath where to save it
   * @param acceptAllSslCerts if we should accept all certificates
   * @throws Exception
   */
  public static void downloadFile(String url, final String targetPath, Boolean acceptAllSslCerts) throws Exception {
    Request request = new Request.Builder().url(url).build();

    // execute it..
    Response response = getDownloadClient(acceptAllSslCerts).newCall(request).execute();

    try (ResponseBody body = response.body()) {
      if (response.code() < 200 || response.code() > 300) {
        throw new Exception(
            "Download returned an unexpected status code of " + response.code());
      }

      File file = new File(targetPath);
      BufferedSink sink = Okio.buffer(Okio.sink(file));

      sink.writeAll(body.source());
      sink.flush();
      sink.close();
    } catch (Exception e) {
      throw new Exception("Error downloading URL '" + url + "'", e);
    }
  }
}
