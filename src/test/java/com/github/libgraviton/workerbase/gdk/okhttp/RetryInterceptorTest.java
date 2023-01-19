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

import java.net.ConnectException;
import java.net.UnknownHostException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class RetryInterceptorTest {

    @RegisterExtension
    public static WorkerTestExtension workerTestExtension = (new WorkerTestExtension())
            .setStartWiremock(true);

    private OkHttpClient client;

    @Test
    public void testWrongHostHandling() {
        Assertions.assertThrows(UnknownHostException.class, () -> {
            RetryInterceptor retryInterceptor = new RetryInterceptor(5, 1);

            client = new OkHttpClient.Builder()
                    .addInterceptor(retryInterceptor)
                    .build();

            Request request = new Request.Builder()
                    .url("http://myservice/my-precious-url/4")
                    .build();

            try {
                client.newCall(request).execute();
            } catch (UnknownHostException e) {
                Assertions.assertEquals(Integer.valueOf(5), retryInterceptor.getRetried());
                throw e;
            }
        });
    }

    @Test
    public void testWrongPortHandling() throws Exception {
        Assertions.assertThrows(ConnectException.class, () -> {
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
                Assertions.assertEquals(Integer.valueOf(5), retryInterceptor.getRetried());
                throw e;
            }
        });
    }

    @Test
    public void testWrongHttpStatusHandling() throws Exception {

        /******* CREATE STUBS -> we will fail 3 times! *****/
        workerTestExtension.getWireMockServer().stubFor(get(urlEqualTo("/service"))
            .inScenario("test")
            .whenScenarioStateIs("SUCCESS")
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("YEEEEES!")
            )
            .willSetStateTo(Scenario.STARTED)
        );

        // failures!
        workerTestExtension.getWireMockServer().stubFor(options(urlEqualTo("/"))
            .inScenario("test")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(503)
            )
            .willSetStateTo("ERROR1")
        );
        workerTestExtension.getWireMockServer().stubFor(options(urlEqualTo("/"))
            .inScenario("test")
            .whenScenarioStateIs("ERROR1")
            .willReturn(aResponse()
                .withStatus(503)
            )
            .willSetStateTo("ERROR2")
        );
        workerTestExtension.getWireMockServer().stubFor(options(urlEqualTo("/"))
            .inScenario("test")
            .whenScenarioStateIs("ERROR2")
            .willReturn(aResponse()
                .withStatus(503)
            )
            .willSetStateTo("ERROR3")
        );
        workerTestExtension.getWireMockServer().stubFor(options(urlEqualTo("/"))
            .inScenario("test")
            .whenScenarioStateIs("ERROR2")
            .willReturn(aResponse()
                .withStatus(204)
            )
            .willSetStateTo("SUCCESS")
        );

        /****** START TEST ******/

        RetryInterceptor retryInterceptor = new RetryInterceptor(5, 1);

        client = new OkHttpClient.Builder()
            .addInterceptor(retryInterceptor)
            .build();

        Request request = new Request.Builder()
            .url(workerTestExtension.getWireMockServer().baseUrl()+"/service")
            .build();

        Response response = client.newCall(request).execute();

        Assertions.assertEquals(Integer.valueOf(2), retryInterceptor.getRetried());
        Assertions.assertEquals(200, response.code());
        Assertions.assertEquals("YEEEEES!", response.body().string());
    }

}
