package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.NoopRequest;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint;
import com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointManager;
import com.github.libgraviton.workerbase.gdk.api.header.Header;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.query.rql.Rql;
import com.github.libgraviton.workerbase.gdk.data.NoopClass;
import com.github.libgraviton.workerbase.gdk.data.SimpleClass;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import com.github.libgraviton.workerbase.gdk.serialization.JsonPatcher;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.RqlObjectMapper;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GravitonApiTest {

    private GravitonApi gravitonApi;

    private final String itemUrl = "http://someUrl/item123";

    private final String endpointUrl = "http://someUrl/";

    private String baseUrl = "someBaseUrl";

    @BeforeEach
    public void setupService() throws Exception {
        EndpointManager endpointManager = mock(EndpointManager.class);
        Endpoint endpoint = mock(Endpoint.class);
        when(endpoint.getItemUrl()).thenReturn(itemUrl);
        when(endpoint.getUrl()).thenReturn(endpointUrl);
        when(endpointManager.getEndpoint(anyString())).thenReturn(endpoint);

        gravitonApi = spy(GravitonApi.getInstance(
                endpointManager,
                DependencyInjection.getInstance(ObjectMapper.class),
                DependencyInjection.getInstance(RqlObjectMapper.class)
        ));
    }

    @Test
    public void testGet() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi.get(resource).build();
        Assertions.assertEquals(itemUrl, request.getUrl().toString());
        Assertions.assertEquals(HttpMethod.GET, request.getMethod());
    }

    @Test
    public void testQuery() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi
                .query(resource)
                .setQuery(new Rql.Builder().setLimit(1).build())
                .build();
        Assertions.assertEquals("http://someUrl/?eq(id,string:111)&limit(1)", request.getUrl().toString());
        Assertions.assertEquals(HttpMethod.GET, request.getMethod());
    }

    @Test
    public void testGetDefaultHeader() {
        HeaderBag headers = gravitonApi.getDefaultHeaders().build();
        Assertions.assertEquals(3, headers.all().size());

        Header header = headers.get("x-graviton-authentication");
        Assertions.assertEquals("subnet-java-test", header.get(0));
    }

    @Test
    public void testPut() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi.put(resource).build();
        Assertions.assertEquals(itemUrl, request.getUrl().toString());
        Assertions.assertEquals(HttpMethod.PUT, request.getMethod());
        Assertions.assertNotNull(request.getBody());
    }

    @Test
    public void testPatch() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        JsonPatcher.add(resource, gravitonApi.getObjectMapper().valueToTree(resource));

        Request request = gravitonApi.patch(resource).build();
        Assertions.assertTrue(request instanceof NoopRequest);

        resource.setName("aName");
        request = gravitonApi.patch(resource).build();
        Assertions.assertFalse(request instanceof NoopRequest);
        Assertions.assertEquals(itemUrl, request.getUrl().toString());
        Assertions.assertEquals(HttpMethod.PATCH, request.getMethod());
        Assertions.assertNotNull(request.getBody());
    }

    @Test
    public void testPost() throws Exception {
        SimpleClass resource = new SimpleClass();
        Request request = gravitonApi.post(resource).build();
        Assertions.assertEquals(endpointUrl, request.getUrl().toString());
        Assertions.assertEquals(HttpMethod.POST, request.getMethod());
        Assertions.assertNotNull(request.getBody());
    }

    @Test
    public void testPostWithFailedSerialization() {
        Assertions.assertThrows(SerializationException.class, () -> {
            ObjectMapper mapper = mock(ObjectMapper.class);
            when(mapper.writeValueAsString(any(Object.class))).thenThrow(JsonProcessingException.class);
            doReturn(mapper).when(gravitonApi).getObjectMapper();

            SimpleClass resource = new SimpleClass();
            gravitonApi.post(resource).build();
        });
    }

    @Test
    public void testDelete() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi.delete(resource).build();
        Assertions.assertEquals(itemUrl, request.getUrl().toString());
        Assertions.assertEquals(HttpMethod.DELETE, request.getMethod());
    }

    @Test
    public void testHead() throws Exception {
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Request request = gravitonApi.head(resource).build();
        Assertions.assertEquals(endpointUrl, request.getUrl().toString());
        Assertions.assertEquals(HttpMethod.HEAD, request.getMethod());
    }

    @Test
    public void testOptions() throws Exception {
        SimpleClass resource = new SimpleClass();
        Request request = gravitonApi.options(resource).build();
        Assertions.assertEquals(endpointUrl, request.getUrl().toString());
        Assertions.assertEquals(HttpMethod.OPTIONS, request.getMethod());
    }

    @Test
    public void testExtractId() throws Exception {
        NoopClass resourceWithoutId = new NoopClass();
        SimpleClass resource = new SimpleClass();
        resource.setId("111");
        Assertions.assertEquals("", gravitonApi.extractId(resourceWithoutId));
        Assertions.assertEquals("111", gravitonApi.extractId(resource));
    }
}
