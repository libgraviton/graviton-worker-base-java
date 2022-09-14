package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.NoopRequest;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint;
import com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointManager;
import com.github.libgraviton.workerbase.gdk.api.query.rql.Rql;
import com.github.libgraviton.workerbase.gdk.data.NoopClass;
import com.github.libgraviton.workerbase.gdk.data.SimpleClass;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import com.github.libgraviton.workerbase.gdk.serialization.JsonPatcher;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GravitonApiTest {

    private GravitonApi gravitonApi;

    private final String itemUrl = "http://someUrl/item123";

    private final String endpointUrl = "http://someUrl/";

    private String baseUrl = "someBaseUrl";

    @Before
    public void setupService() throws Exception {
        EndpointManager endpointManager = mock(EndpointManager.class);
        Endpoint endpoint = mock(Endpoint.class);
        when(endpoint.getItemUrl()).thenReturn(itemUrl);
        when(endpoint.getUrl()).thenReturn(endpointUrl);
        when(endpointManager.getEndpoint(anyString())).thenReturn(endpoint);

        gravitonApi = spy(new GravitonApi(baseUrl, endpointManager));
    }

    @Test
    public void testGet() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi.get(resource).build();
        assertEquals(itemUrl, request.getUrl().toString());
        assertEquals(HttpMethod.GET, request.getMethod());
    }

    @Test
    public void testQuery() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi
                .query(resource)
                .setQuery(new Rql.Builder().setLimit(1).build())
                .build();
        assertEquals("http://someUrl/?eq(id,string:111)&limit(1)", request.getUrl().toString());
        assertEquals(HttpMethod.GET, request.getMethod());
    }

    @Test
    public void testPut() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi.put(resource).build();
        assertEquals(itemUrl, request.getUrl().toString());
        assertEquals(HttpMethod.PUT, request.getMethod());
        assertNotNull(request.getBody());
    }

    @Test
    public void testPatch() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        JsonPatcher.add(resource, gravitonApi.getObjectMapper().valueToTree(resource));

        Request request = gravitonApi.patch(resource).build();
        assertTrue(request instanceof NoopRequest);

        resource.setName("aName");
        request = gravitonApi.patch(resource).build();
        assertFalse(request instanceof NoopRequest);
        assertEquals(itemUrl, request.getUrl().toString());
        assertEquals(HttpMethod.PATCH, request.getMethod());
        assertNotNull(request.getBody());
    }

    @Test
    public void testPost() throws Exception {
        SimpleClass resource = new SimpleClass();
        Request request = gravitonApi.post(resource).build();
        assertEquals(endpointUrl, request.getUrl().toString());
        assertEquals(HttpMethod.POST, request.getMethod());
        assertNotNull(request.getBody());
    }

    @Test(expected = SerializationException.class)
    public void testPostWithFailedSerialization() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        when(mapper.writeValueAsString(any(Object.class))).thenThrow(JsonProcessingException.class);
        doReturn(mapper).when(gravitonApi).getObjectMapper();

        SimpleClass resource = new SimpleClass();
        gravitonApi.post(resource).build();
    }

    @Test
    public void testDelete() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi.delete(resource).build();
        assertEquals(itemUrl, request.getUrl().toString());
        assertEquals(HttpMethod.DELETE, request.getMethod());
    }

    @Test
    public void testHead() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi.head(resource).build();
        assertEquals(endpointUrl, request.getUrl().toString());
        assertEquals(HttpMethod.HEAD, request.getMethod());
    }

    @Test
    public void testOptions() throws Exception {
        SimpleClass resource = new SimpleClass();
        Request request = gravitonApi.options(resource).build();
        assertEquals(endpointUrl, request.getUrl().toString());
        assertEquals(HttpMethod.OPTIONS, request.getMethod());
    }

    @Test
    public void testExtractId() throws Exception {
        NoopClass resourceWithoutId = new NoopClass();
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        assertEquals("", gravitonApi.extractId(resourceWithoutId));
        assertEquals("111", gravitonApi.extractId(resource));
    }
}
