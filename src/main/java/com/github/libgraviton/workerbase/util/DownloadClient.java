package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workerbase.helper.WorkerUtil;
import java.io.File;
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
