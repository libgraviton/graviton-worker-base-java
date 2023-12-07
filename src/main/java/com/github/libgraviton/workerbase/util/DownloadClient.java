package com.github.libgraviton.workerbase.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.github.libgraviton.workerbase.helper.DependencyInjection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadClient {

  private static final Logger LOG = LoggerFactory.getLogger(DownloadClient.class);

  /**
   * Downloads a file to disk
   *
   * @param url url to get
   * @param targetPath where to save it
   * @throws Exception
   */
  public static void downloadFile(String url, final String targetPath, Map<String, String> requestHeaders) {
    try {
      HttpClient httpClient = DependencyInjection.getInstance(HttpClient.class);

      HttpRequest.Builder reqBuilder = HttpRequest.newBuilder(new URI(url));
      requestHeaders.forEach(reqBuilder::header);

      httpClient.send(
        reqBuilder.build(),
        HttpResponse.BodyHandlers.ofFile(Path.of(targetPath))
      );

      LOG.info("Downloaded '{}' to path '{}'", url, targetPath);
    } catch (Throwable t) {
      LOG.error("Error downloading url '{}'", url, t);
    }
  }

  public static void downloadFile(String url, final String targetPath) {
    downloadFile(url, targetPath, Map.of());
  }

  @Deprecated(since = "Use writeFileContentToDisk(), File and streams to deal with files, not byte arrays!")
  public static byte[] downloadFileBytes(String url) throws IOException {
    java.io.File finalTemp = java.io.File.createTempFile("grv-file", ".tmp");

    // download
    downloadFile(url, finalTemp.getAbsolutePath(), Map.of());

    if (!finalTemp.exists()) {
      LOG.error("URL '{}' could not be downloaded", url);
      throw new IOException("Could not download URL");
    }

    byte[] contents = Files.readAllBytes(finalTemp.toPath());

    // delete file
    finalTemp.delete();

    // read into array
    return contents;
  }

}
