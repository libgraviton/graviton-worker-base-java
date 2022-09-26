package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workerbase.helper.WorkerUtil;
import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadClient {

  private static final Logger LOG = LoggerFactory.getLogger(DownloadClient.class);

  public static OkHttpClient getDownloadClient(Boolean acceptAllSslCerts) {
    OkHttpClient okHttpClient;

    if (acceptAllSslCerts) {
      try {
        okHttpClient = WorkerUtil.getAllTrustingGatewayInstance().getOkHttp();
      } catch (Exception e) {
        LOG.error("Unable to construct all trusting OkHttpClient instance - falling back to normal one.");
        okHttpClient = WorkerUtil.getGatewayInstance().getOkHttp();
      }
    } else {
      okHttpClient = WorkerUtil.getGatewayInstance().getOkHttp();
    }

    Builder builder = okHttpClient.newBuilder()
        .retryOnConnectionFailure(true);

    return builder.build();
  }

  /**
   * Downloads a file to disk
   *
   * @param url url to get
   * @param targetPath where to save it
   * @param acceptAllSslCerts if we should accept all certificates
   * @throws Exception
   */
  public static void downloadFile(String url, final String targetPath, boolean acceptAllSslCerts) {
    try (ResponseBody body = getResponseBody(url, acceptAllSslCerts)) {
      File file = new File(targetPath);
      BufferedSink sink = Okio.buffer(Okio.sink(file));

      sink.writeAll(body.source());
      sink.flush();
      sink.close();
    } catch (Exception e) {
      throw new RuntimeException("Error downloading URL '" + url + "'", e);
    }
  }

  @Deprecated(since = "Use writeFileContentToDisk(), File and streams to deal with files, not byte arrays!")
  public static byte[] downloadFileBytes(String url, boolean acceptAllSslCerts) throws IOException {
    try (ResponseBody body = getResponseBody(url, acceptAllSslCerts)) {
      return body.bytes();
    }
  }

  private static ResponseBody getResponseBody(String url, Boolean acceptAllSslCerts) throws IOException {
    Request request = new Request.Builder().url(url).build();
    Response response = getDownloadClient(acceptAllSslCerts).newCall(request).execute();

    if (response.code() < 200 || response.code() > 300) {
      throw new RuntimeException(
              "Download of url '" + url + "' returned an unexpected status code of " + response.code());
    }

    return response.body();
  }
}
