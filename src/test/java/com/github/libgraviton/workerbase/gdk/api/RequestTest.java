package com.github.libgraviton.workerbase.gdk.api;

import com.github.libgraviton.workerbase.gdk.RequestExecutor;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulRequestException;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestTest {

    private Request.Builder builder;

    private Response response;

    @Before
    public void setup() throws Exception {
        response = mock(Response.class);

        RequestExecutor executor = mock(RequestExecutor.class);
        when(executor.execute(any(Request.class))).thenReturn(response);
        URL url = new URL("http://aRandomUrl");
        builder = new Request.Builder(executor).setUrl(url);
    }

    @Test
    public void testBuilderGetUrl() throws Exception {
        builder.setUrl("http://someUrl/{id}").addParam("id","someId");
        assertEquals("http://someUrl/someId", builder.buildUrl().toString());
    }

    @Test
    public void testGet() throws Exception {
        Request request = builder.get().build();
        assertEquals(HttpMethod.GET, request.getMethod());
        assertNull(request.getBody());
    }

    @Test
    public void testPut() throws Exception {
        String data = "putData";
        Request request = builder.put(data).build();
        assertEquals(HttpMethod.PUT, request.getMethod());
        assertEquals(data, request.getBody());
    }

    @Test
    public void testPost() throws Exception {
        String data = "postData";
        Request request = builder.post(data).build();
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals(data, request.getBody());
    }

    @Test
    public void testPatch() throws Exception {
        String data = "patchData";
        Request request = builder.patch(data).build();
        assertEquals(HttpMethod.PATCH, request.getMethod());
        assertEquals(data, request.getBody());
    }

    @Test
    public void testDelete() throws Exception {
        Request request = builder.delete().build();
        assertEquals(HttpMethod.DELETE, request.getMethod());
        assertNull(request.getBody());
    }

    @Test
    public void testHead() throws Exception {
        Request request = builder.head().build();
        assertEquals(HttpMethod.HEAD, request.getMethod());
        assertNull(request.getBody());
    }

    @Test
    public void testOptions() throws Exception {
        Request request = builder.options().build();
        assertEquals(HttpMethod.OPTIONS, request.getMethod());
        assertNull(request.getBody());
    }

    @Test
    public void testMultipartPost() throws Exception {
        String formName = "something";
        String body1 = "body part 1";
        String body2 = "body part 2";
        Part part1 = new Part(body1, formName);
        Part part2 = new Part(body2);

        Request request = builder.post(part1, part2).build();
        assertEquals(HttpMethod.POST, request.getMethod());
        List<Part> parts = request.getParts();
        assertEquals(2, parts.size());
        assertEquals(part1, parts.get(0));
        assertEquals(part2, parts.get(1));
    }

    @Test
    public void testMultipartPut() throws Exception {
        String body = "body part ";
        Part part = new Part(body);

        Request request = builder.put(part).build();
        assertEquals(HttpMethod.PUT, request.getMethod());
        List<Part> parts = request.getParts();
        assertEquals(1, parts.size());
        assertEquals(part, parts.get(0));
    }

    @Test
    public void testParams() throws Exception {
        String param1 = "param1";
        String param2 = "param2";
        String value1 = "value1";
        String value2 = "value2";
        assertEquals(0, builder.getParams().size());
        builder.addParam(param1, value1);
        assertEquals(1, builder.getParams().size());
        assertEquals(value1, builder.getParams().get(param1));

        builder.addParam(param2, value1);
        assertEquals(2, builder.getParams().size());
        assertEquals(value1, builder.getParams().get(param2));

        Map<String, String> params = new HashMap<>();
        params.put(param2, value2);
        builder.setParams(params);
        assertEquals(1, builder.getParams().size());
        assertEquals(value2, builder.getParams().get(param2));
    }

    @Test
    public void testHeaders() throws Exception {
        String param1 = "param1";
        String param2 = "param2";
        String value1 = "value1";
        String value2 = "value2";
        Request request = builder.build();
        assertEquals(0, request.getHeaders().all().size());
        request = builder.addHeader(param1, value1).build();
        assertEquals(1, request.getHeaders().all().size());
        assertEquals(value1, request.getHeaders().get(param1).get(0));

        request = builder.addHeader(param2, value2).build();
        assertEquals(2, request.getHeaders().all().size());
        assertEquals(value2, request.getHeaders().get(param2).get(0));

        request = builder.setHeaders(null).build();
        assertEquals(0, request.getHeaders().all().size());
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        Response actualResponse = builder.get().execute();
        assertEquals(response, actualResponse);
    }

    @Test(expected = UnsuccessfulRequestException.class)
    public void testExecuteFail() throws Exception {
        builder.setUrl("malformed-url");
        Response actualResponse = builder.get().execute();
        assertEquals(response, actualResponse);
    }
}
