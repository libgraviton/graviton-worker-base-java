package com.github.libgraviton.workerbase.gdk;

import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint;
import com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointManager;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.data.SimpleClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class GravitonFileEndpointTest {

    private GravitonApi gravitonApi;

    private GravitonFileEndpoint gravitonFileEndpoint;

    private String url = "http://someUrl";

    private String itemUrl = "http://someUrl/{id}";

    private SimpleClass resource;

    @Before
    public void setupService() throws Exception {
        EndpointManager endpointManager = mock(EndpointManager.class);
        Endpoint endpoint = mock(Endpoint.class);
        when(endpoint.getUrl()).thenReturn(url);
        when(endpoint.getItemUrl()).thenReturn(itemUrl);
        when(endpointManager.getEndpoint(anyString())).thenReturn(endpoint);

        gravitonApi = mock(GravitonApi.class);
        when(gravitonApi.request()).thenCallRealMethod();
        when(gravitonApi.get(url)).thenCallRealMethod();
        when(gravitonApi.getEndpointManager()).thenReturn(endpointManager);
        when(gravitonApi.extractId(any(GravitonBase.class))).thenCallRealMethod();
        when(gravitonApi.serializeResource(any(SimpleClass.class))).thenReturn("{ \"id\":\"111\"}");
        HeaderBag.Builder headers = new HeaderBag.Builder()
                .set("whatever", "something")
                .set("Accept", "almost-everything");

        when(gravitonApi.getDefaultHeaders()).thenReturn(headers);
        resource = new SimpleClass();
        resource.setId("111");

        gravitonFileEndpoint = new GravitonFileEndpoint(gravitonApi);
    }

    @Test
    public void testGetFile() throws Exception {
        Request request = gravitonFileEndpoint.getFile(url).build();
        assertEquals(0, request.getHeaders().get("Accept").all().size());
        assertEquals(1, request.getHeaders().get("whatever").all().size());
    }

    @Test
    public void testGetMetadata() throws Exception {
        Request request = gravitonFileEndpoint.getMetadata(url).build();
        assertEquals(1, request.getHeaders().get("whatever").all().size());
        verify(gravitonApi, times(1)).get(url);
    }

    @Test
    public void testPost() throws Exception {
        String data = "some real data";

        Request request = gravitonFileEndpoint.post(data.getBytes(), resource).build();
        List<Part> parts = request.getParts();
        assertEquals(2, parts.size());

        Part part1 = parts.get(0);
        assertEquals("upload", part1.getFormName());
        assertEquals(data, new String(part1.getBody()));

        Part part2 = parts.get(1);
        assertEquals("metadata", part2.getFormName());
        assertEquals("{ \"id\":\"111\"}", new String(part2.getBody()));
    }

    @Test
    public void testPut() throws Exception {
        String data = "some real data";

        Request request = gravitonFileEndpoint.put(data.getBytes(), resource).build();
        List<Part> parts = request.getParts();
        assertEquals(2, parts.size());

        Part part1 = parts.get(0);
        assertEquals("upload", part1.getFormName());
        assertEquals(data, new String(part1.getBody()));

        Part part2 = parts.get(1);
        assertEquals("metadata", part2.getFormName());
        assertEquals("{ \"id\":\"111\"}", new String(part2.getBody()));
    }

    @Test
    public void testPatch() throws Exception {
        SimpleClass resource = new SimpleClass();
        gravitonFileEndpoint.patch(resource);
        verify(gravitonApi, times(1)).patch(resource);
    }

    @Test
    public void testDelete() throws Exception {
        SimpleClass resource = new SimpleClass();
        gravitonFileEndpoint.delete(resource);
        verify(gravitonApi, times(1)).delete(resource);
    }
}
