package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import java.io.File;
import java.io.IOException;

import io.activej.inject.annotation.Inject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

@GravitonWorkerDiScan
public class DownloadClient {

  @Inject
  static OkHttpClient client;

  /**
   * Downloads a file to disk
   *
   * @param url url to get
   * @param targetPath where to save it
   * @throws Exception
   */
  public static void downloadFile(String url, final String targetPath) {
    try (ResponseBody body = getResponseBody(url)) {
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
  public static byte[] downloadFileBytes(String url) throws IOException {
    try (ResponseBody body = getResponseBody(url)) {
      return body.bytes();
    }
  }

  private static ResponseBody getResponseBody(String url) throws IOException {
    Request request = new Request.Builder().url(url).build();
    Response response = client.newCall(request).execute();

    if (response.code() < 200 || response.code() > 300) {
      throw new RuntimeException(
              "Download of url '" + url + "' returned an unexpected status code of " + response.code());
    }

    return response.body();
  }
}
