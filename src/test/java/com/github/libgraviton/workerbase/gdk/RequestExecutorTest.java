package com.github.libgraviton.workerbase.gdk;

import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.gateway.GravitonGateway;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulResponseException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestExecutorTest {

    private RequestExecutor executor;

    private GravitonGateway gateway;

    @Before
    public void setup() {
        gateway = mock(GravitonGateway.class);
        DependencyInjection.addInstanceOverride(GravitonGateway.class, gateway);
        executor = DependencyInjection.getInstance(RequestExecutor.class);
    }

    @Test
    public void testExecuteSuccessfulResponse() throws CommunicationException {
        Request request = mock(Request.class);
        Response response = mock(Response.class);
        when(response.isSuccessful()).thenReturn(true);
        when(gateway.execute(eq(request))).thenReturn(response);

        Response actualResponse = executor.execute(request);
        Assert.assertEquals(response, actualResponse);
    }

    @Test(expected = UnsuccessfulResponseException.class)
    public void testExecuteUnsuccessfulResponse() throws Exception {
        Request request = mock(Request.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getUrl()).thenReturn(new URL("http://some-test-url"));
        Response response = mock(Response.class);
        when(response.isSuccessful()).thenReturn(false);
        when(response.getRequest()).thenReturn(request);
        when(response.getCode()).thenReturn(500);
        when(response.getMessage()).thenReturn("Oops!");
        when(response.getBody()).thenReturn("Content");
        when(gateway.execute(eq(request))).thenReturn(response);

        Response actualResponse = executor.execute(request);
        Assert.assertEquals(response, actualResponse);
    }
}
