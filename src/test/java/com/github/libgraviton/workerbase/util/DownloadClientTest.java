package com.github.libgraviton.workerbase.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class DownloadClientTest {

  @Test
  public void testBasicDownload() {
    DownloadClient.downloadFile("", "", Map.of());
  }

}
