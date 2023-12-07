package com.github.libgraviton.workerbase.gdk.okhttp;

import com.github.libgraviton.workerbase.gdk.util.http.interceptor.RetryInterceptor;
import com.github.libgraviton.workertestbase.WorkerTestExtension;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MutableRequest;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class RetryInterceptorTest {

    @RegisterExtension
    public static WorkerTestExtension workerTestExtension = (new WorkerTestExtension())
            .setStartWiremock(true);

    private HttpClient client;

    @Test
    public void testWrongHostHandling() {
        Assertions.assertThrows(IOException.class, () -> {
            RetryInterceptor retryInterceptor = new RetryInterceptor(5, 1);

            client = Methanol.newBuilder()
                    .interceptor(retryInterceptor)
                    .build();

            HttpRequest request = MutableRequest.newBuilder(new URI("http://myservice/my-precious-url/4"))
              .build();

            client.send(request, HttpResponse.BodyHandlers.discarding());
        });
    }

    @Test
    public void testWrongPortHandling() throws Exception {
        Assertions.assertThrows(IOException.class, () -> {
            RetryInterceptor retryInterceptor = new RetryInterceptor(5, 1);

            client = Methanol.newBuilder()
              .interceptor(retryInterceptor)
              .build();

            HttpRequest request = MutableRequest.newBuilder(new URI("http://localhost:9988"))
              .build();

            try {
                client.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (ConnectException e) {
                throw e;
            }
        });
    }

    @Test
    public void testWrongHttpStatusHandling() throws Exception {

        workerTestExtension.getWireMockServer().stubFor(
          get(urlEqualTo("/service"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("Second Request")
        );

        workerTestExtension.getWireMockServer().stubFor(
          get(urlEqualTo("/service"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Second Request")
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("Third Request")
        );

        workerTestExtension.getWireMockServer().stubFor(
          get(urlEqualTo("/service"))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Third Request")
            .willReturn(aResponse().withStatus(200).withBody("YEEEEES!"))
        );

        /****** START TEST ******/

        RetryInterceptor retryInterceptor = new RetryInterceptor(5, 1);

        client = Methanol.newBuilder()
          .interceptor(retryInterceptor)
          .build();

        HttpRequest request = MutableRequest.newBuilder(new URI(workerTestExtension.getWiremockUrl() + "/service"))
          .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("YEEEEES!", response.body());
    }

}
