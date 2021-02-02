package com.github.libgraviton.workerbase.gdk.api.endpoint;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EndpointInclusionStrategyTest {

    @Test
    public void testDefaultStrategy() throws Exception {
        EndpointInclusionStrategy strategy = EndpointInclusionStrategy
                .create(EndpointInclusionStrategy.Strategy.DEFAULT, null);

        Endpoint endpoint1 = new Endpoint("http://some-base-url/abc/");
        Endpoint endpoint2 = new Endpoint("http://some-base-url/def/");
        assertEquals(false, strategy.shouldIgnoreEndpoint(endpoint1));
        assertEquals(false, strategy.shouldIgnoreEndpoint(endpoint2));
    }

    @Test
    public void testBlacklistStrategy() throws Exception {
        EndpointInclusionStrategy strategy = EndpointInclusionStrategy
                .create(EndpointInclusionStrategy.Strategy.BLACKLIST, "src/test/resources/endpoint.blacklist");

        Endpoint endpoint1 = new Endpoint("/abc/{id}", "/abc/");
        Endpoint endpoint2 = new Endpoint("/def/{id}", "/def/");
        assertEquals(true, strategy.shouldIgnoreEndpoint(endpoint1));
        assertEquals(false, strategy.shouldIgnoreEndpoint(endpoint2));
    }

    @Test
    public void testWhitelistStrategy() throws Exception {
        EndpointInclusionStrategy strategy = EndpointInclusionStrategy
                .create(EndpointInclusionStrategy.Strategy.WHITELIST, "src/test/resources/endpoint.whitelist");

        Endpoint endpoint1 = new Endpoint("/abc/{id}", "/abc/");
        Endpoint endpoint2 = new Endpoint("/def/{id}", "/def/");
        assertEquals(false, strategy.shouldIgnoreEndpoint(endpoint1));
        assertEquals(true, strategy.shouldIgnoreEndpoint(endpoint2));
    }
}
