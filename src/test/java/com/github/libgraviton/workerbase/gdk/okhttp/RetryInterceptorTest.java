package com.github.libgraviton.workerbase.gdk.okhttp;

import com.github.libgraviton.gdk.util.okhttp.interceptor.RetryInterceptor;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Rule;
import org.junit.Test;

import java.net.ConnectException;
import java.net.UnknownHostException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class RetryInterceptorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    private OkHttpClient client;

    @Test(expected = UnknownHostException.class)
    public void testWrongHostHandling() throws Exception {
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
            assertEquals(new Integer(5), retryInterceptor.getRetried());
            throw e;
        }
    }

    @Test(expected = ConnectException.class)
    public void testWrongPortHandling() throws Exception {
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
            assertEquals(new Integer(5), retryInterceptor.getRetried());
            throw e;
        }
    }

    @Test
    public void testWrongHttpStatusHandling() throws Exception {

        /******* CREATE STUBS -> we will fail 3 times! *****/
        stubFor(get(urlEqualTo("/service"))
            .inScenario("test")
            .whenScenarioStateIs("SUCCESS")
            .willReturn(aResponse()
                .withStatus(200)
                .withBody("YEEEEES!")
            )
            .willSetStateTo(Scenario.STARTED)
        );

        // failures!
        stubFor(options(urlEqualTo("/"))
            .inScenario("test")
            .whenScenarioStateIs(Scenario.STARTED)
            .willReturn(aResponse()
                .withStatus(503)
            )
            .willSetStateTo("ERROR1")
        );
        stubFor(options(urlEqualTo("/"))
            .inScenario("test")
            .whenScenarioStateIs("ERROR1")
            .willReturn(aResponse()
                .withStatus(503)
            )
            .willSetStateTo("ERROR2")
        );
        stubFor(options(urlEqualTo("/"))
            .inScenario("test")
            .whenScenarioStateIs("ERROR2")
            .willReturn(aResponse()
                .withStatus(503)
            )
            .willSetStateTo("ERROR3")
        );
        stubFor(options(urlEqualTo("/"))
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
            .url("http://localhost:8089/service")
            .build();

        Response response = client.newCall(request).execute();

        assertEquals(new Integer(2), retryInterceptor.getRetried());
        assertEquals(200, response.code());
        assertEquals("YEEEEES!", response.body().string());
    }

}
