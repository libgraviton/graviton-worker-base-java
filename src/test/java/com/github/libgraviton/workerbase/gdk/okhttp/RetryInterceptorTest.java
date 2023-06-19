package com.github.libgraviton.workerbase.gdk.okhttp;

import com.github.libgraviton.workerbase.gdk.util.okhttp.interceptor.RetryInterceptor;
import com.github.libgraviton.workertestbase.WorkerTestExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.ConnectException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class RetryInterceptorTest {

    @RegisterExtension
    public static WorkerTestExtension workerTestExtension = (new WorkerTestExtension())
            .setStartWiremock(true);

    private OkHttpClient client;

    @Test
    public void testWrongHostHandling() {
        Assertions.assertThrows(IOException.class, () -> {
            RetryInterceptor retryInterceptor = new RetryInterceptor(5, 1);

            client = new OkHttpClient.Builder()
                    .addInterceptor(retryInterceptor)
                    .build();

            Request request = new Request.Builder()
                    .url("http://myservice/my-precious-url/4")
                    .build();

            client.newCall(request).execute();
        });
    }

    @Test
    public void testWrongPortHandling() throws Exception {
        Assertions.assertThrows(IOException.class, () -> {
            RetryInterceptor retryInterceptor = new RetryInterceptor(5, 1);

            client = new OkHttpClient.Builder()
                    .addInterceptor(retryInterceptor)
                    .build();

            Request request = new Request.Builder()
                    .url("http://localhost:9988")
                    .build();

            try {
                client.newCall(request).execute();
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

        client = new OkHttpClient.Builder()
            .addInterceptor(retryInterceptor)
            .build();

        Request request = new Request.Builder()
            .url(workerTestExtension.getWiremockUrl() + "/service")
            .build();

        Response response = client.newCall(request).execute();

        Assertions.assertEquals(200, response.code());
        Assertions.assertEquals("YEEEEES!", response.body().string());
    }

}
