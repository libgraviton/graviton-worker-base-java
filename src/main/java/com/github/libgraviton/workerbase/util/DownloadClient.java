package com.github.libgraviton.workerbase.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.github.libgraviton.workerbase.helper.DependencyInjection;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;

public class DownloadClient {

  /**
   * Downloads a file to disk
   *
   * @param url url to get
   * @param targetPath where to save it
   * @throws Exception
   */
  public static void downloadFile(String url, final String targetPath, Map<String, String> requestHeaders) {
    try (ResponseBody body = getResponseBody(url, requestHeaders)) {
      File file = new File(targetPath);
      BufferedSink sink = Okio.buffer(Okio.sink(file));

      sink.writeAll(body.source());
      sink.flush();
      sink.close();
    } catch (Exception e) {
      throw new RuntimeException("Error downloading URL '" + url + "'", e);
    }
  }

  public static void downloadFile(String url, final String targetPath) {
    downloadFile(url, targetPath, Map.of());
  }

  @Deprecated(since = "Use writeFileContentToDisk(), File and streams to deal with files, not byte arrays!")
  public static byte[] downloadFileBytes(String url) throws IOException {
    try (ResponseBody body = getResponseBody(url, Map.of())) {
      return body.bytes();
    }
  }

  private static ResponseBody getResponseBody(String url, Map<String, String> requestHeaders) throws IOException {
    Request request = new Request.Builder().url(url).headers(Headers.of(requestHeaders)).build();

    OkHttpClient client = DependencyInjection.getInstance(OkHttpClient.class);
    Response response = client.newCall(request).execute();

    if (response.code() < 200 || response.code() > 300) {
      throw new RuntimeException(
              "Download of url '" + url + "' returned an unexpected status code of " + response.code());
    }

    return response.body();
  }
}
