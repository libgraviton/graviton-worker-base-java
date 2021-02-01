package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.gdk.util.PropertiesLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class EndpointTest {

    private Endpoint endpoint;

    @Before
    public void setupService() {
        endpoint = new Endpoint("endpoint://item", "endpoint://collection/");
    }

    @Test
    public void testEqualsSameNotNull() {
        Endpoint equalEndpoint = new Endpoint("endpoint://item", "endpoint://collection/");
        assertTrue(endpoint.equals(equalEndpoint));
        assertTrue(equalEndpoint.equals(endpoint));
    }

    @Test
    public void testEqualsSameNull() {
        Endpoint endpoint = new Endpoint(null, null);
        Endpoint equalEndpoint = new Endpoint(null, null);
        assertTrue(endpoint.equals(equalEndpoint));
        assertTrue(equalEndpoint.equals(endpoint));
    }

    @Test
    public void testDifferentNotNull() {
        Endpoint equalEndpoint = new Endpoint("endpoint://other/item", "endpoint://other/collection/");
        assertFalse(endpoint.equals(equalEndpoint));
        assertFalse(equalEndpoint.equals(endpoint));
    }

    @Test
    public void testDifferentNull() {
        Endpoint equalEndpoint = new Endpoint(null, null);
        assertFalse(endpoint.equals(equalEndpoint));
        assertFalse(equalEndpoint.equals(endpoint));
    }

    @Test
    public void testEndpointUrls() throws Exception {
        Properties properties = PropertiesLoader.load();
        String baseUrl = properties.getProperty("graviton.base.url");
        assertEquals(baseUrl, Endpoint.getBaseUrl());

        String itemPath = "/other/item";
        String path = "/other/collection/";
        Endpoint endpoint = new Endpoint(itemPath, path);

        assertEquals(baseUrl + itemPath, endpoint.getItemUrl());
        assertEquals(baseUrl + path, endpoint.getUrl());
        assertEquals(path, endpoint.getPath());
        assertEquals(itemPath, endpoint.getItemPath());

        endpoint = new Endpoint(itemPath);
        assertEquals(baseUrl + itemPath, endpoint.getItemUrl());
        assertEquals(itemPath, endpoint.getItemPath());
        assertNull(endpoint.getUrl());
        assertNull(endpoint.getPath());
    }
}
