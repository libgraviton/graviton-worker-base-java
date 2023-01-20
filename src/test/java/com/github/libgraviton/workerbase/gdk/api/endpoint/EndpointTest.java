package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.helper.WorkerProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class EndpointTest {

    private Endpoint endpoint;

    @BeforeEach
    public void setupService() {
        endpoint = new Endpoint("endpoint://item", "endpoint://collection/");
    }

    @Test
    public void testEqualsSameNotNull() {
        Endpoint equalEndpoint = new Endpoint("endpoint://item", "endpoint://collection/");
        Assertions.assertTrue(endpoint.equals(equalEndpoint));
        Assertions.assertTrue(equalEndpoint.equals(endpoint));
    }

    @Test
    public void testEqualsSameNull() {
        Endpoint endpoint = new Endpoint(null, null);
        Endpoint equalEndpoint = new Endpoint(null, null);
        Assertions.assertTrue(endpoint.equals(equalEndpoint));
        Assertions.assertTrue(equalEndpoint.equals(endpoint));
    }

    @Test
    public void testDifferentNotNull() {
        Endpoint equalEndpoint = new Endpoint("endpoint://other/item", "endpoint://other/collection/");
        Assertions.assertFalse(endpoint.equals(equalEndpoint));
        Assertions.assertFalse(equalEndpoint.equals(endpoint));
    }

    @Test
    public void testDifferentNull() {
        Endpoint equalEndpoint = new Endpoint(null, null);
        Assertions.assertFalse(endpoint.equals(equalEndpoint));
        Assertions.assertFalse(equalEndpoint.equals(endpoint));
    }

    @Test
    public void testEndpointUrls() throws Exception {
        String baseUrl = WorkerProperties.GRAVITON_BASE_URL.get();
        Assertions.assertEquals(baseUrl, Endpoint.getBaseUrl());

        String itemPath = "/other/item";
        String path = "/other/collection/";
        Endpoint endpoint = new Endpoint(itemPath, path);

        Assertions.assertEquals(baseUrl + itemPath, endpoint.getItemUrl());
        Assertions.assertEquals(baseUrl + path, endpoint.getUrl());
        Assertions.assertEquals(path, endpoint.getPath());
        Assertions.assertEquals(itemPath, endpoint.getItemPath());

        endpoint = new Endpoint(itemPath);
        Assertions.assertEquals(baseUrl + itemPath, endpoint.getItemUrl());
        Assertions.assertEquals(itemPath, endpoint.getItemPath());
        Assertions.assertNull(endpoint.getUrl());
        Assertions.assertNull(endpoint.getPath());
    }
}
