package com.github.libgraviton.workerbase.util;

import com.github.libgraviton.workertestbase.WorkerTestExtension;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class DownloadClientTest {

  @RegisterExtension
  public static WorkerTestExtension workerTestExtension = (new WorkerTestExtension())
    .setStartWiremock(true);

  @Test
  public void testBasicDownload() {
    String target = "/tmp/PDF-TEST-FILE.pdf";
    String url = workerTestExtension.getWiremockUrl() + "/test.pdf";

    StubMapping redir = workerTestExtension.getWireMockServer()
      .stubFor(
        get(urlMatching("/test.pdf"))
          .withHeader("FRANZ", equalTo("TEST"))
          .willReturn(
            aResponse().withStatus(301).withHeader("location", "/file/test.pdf")
          )
      );

    StubMapping filestub = workerTestExtension.getWireMockServer()
      .stubFor(
        get(urlMatching("/file/test.pdf"))
          // the transient headers
          .willReturn(
            aResponse().withBodyFile("test.pdf").withStatus(200)
          )
      );

    DownloadClient.downloadFile(url, target, Map.of("FRANZ", "TEST"));

    File file = new File(target);
    Assertions.assertEquals(7021, file.length());
    file.delete();
  }

}
