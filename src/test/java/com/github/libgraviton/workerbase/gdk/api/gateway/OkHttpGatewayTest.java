package com.github.libgraviton.workerbase.gdk.api.gateway;


import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OkHttpGatewayTest {

    private GravitonGateway gateway;

    private Request request;

    private okhttp3.Response okHttpResponse;

    private Call call;

    private ResponseBody body;

    private String responseBody = "some random okHttpResponse setBody";

    @Before
    public void setup() throws Exception {
        gateway = DependencyInjection.getInstance(GravitonGateway.class);
        Assertions.assertTrue(gateway instanceof OkHttpGateway);

        OkHttpClient client = ((OkHttpGateway) gateway).getOkHttp();

        okHttpResponse = mock(okhttp3.Response.class);
        when(okHttpResponse.headers()).thenReturn(new Headers.Builder().build());
        request = new Request.Builder()
                .setMethod(HttpMethod.GET)
                .setUrl("http://someUrl")
                .build();

        // mock client.newCall()
        call = mock(Call.class);
        when(call.execute()).thenReturn(okHttpResponse);
        when(client.newCall(any(okhttp3.Request.class))).thenReturn(call);

        // mock okHttpResponse.setBody().string()
        body = mock(ResponseBody.class);
        when(body.bytes()).thenReturn(responseBody.getBytes());
        when(okHttpResponse.body()).thenReturn(body);
    }

    @Test
    public void testDoRequestHappyPath() throws CommunicationException {
        when(okHttpResponse.isSuccessful()).thenReturn(true);

        com.github.libgraviton.workerbase.gdk.api.Response response = gateway.execute(request);
        assertEquals(responseBody, response.getBody());
    }

    @Test(expected = CommunicationException.class)
    public void testDoRequestFailedCall() throws CommunicationException {
        try {
            when(call.execute()).thenThrow(new IOException("The call went wrong, but that's ok."));
        } catch (IOException e) {
            e.printStackTrace();
        }

        gateway.execute(request);
    }

    @Test(expected = CommunicationException.class)
    public void testDoRequestCorruptResponseBody() throws CommunicationException {
        try {
            when(body.bytes()).thenThrow(new IOException("Response body is corrupt!"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        gateway.execute(request);
    }
}
