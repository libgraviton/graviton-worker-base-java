package com.github.libgraviton.workerbase.gdk.api.endpoint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EndpointInclusionStrategyTest {

    @Test
    public void testDefaultStrategy() {
        EndpointInclusionStrategy strategy = EndpointInclusionStrategy
                .create(EndpointInclusionStrategy.Strategy.DEFAULT, null);

        Endpoint endpoint1 = new Endpoint("http://some-base-url/abc/");
        Endpoint endpoint2 = new Endpoint("http://some-base-url/def/");
        Assertions.assertFalse(strategy.shouldIgnoreEndpoint(endpoint1));
        Assertions.assertFalse(strategy.shouldIgnoreEndpoint(endpoint2));
    }

    @Test
    public void testBlacklistStrategy() {
        EndpointInclusionStrategy strategy = EndpointInclusionStrategy
                .create(EndpointInclusionStrategy.Strategy.BLACKLIST, "src/test/resources/endpoint.blacklist");

        Endpoint endpoint1 = new Endpoint("/abc/{id}", "/abc/");
        Endpoint endpoint2 = new Endpoint("/def/{id}", "/def/");
        Assertions.assertTrue(strategy.shouldIgnoreEndpoint(endpoint1));
        Assertions.assertFalse(strategy.shouldIgnoreEndpoint(endpoint2));
    }

    @Test
    public void testWhitelistStrategy() {
        EndpointInclusionStrategy strategy = EndpointInclusionStrategy
                .create(EndpointInclusionStrategy.Strategy.WHITELIST, "src/test/resources/endpoint.whitelist");

        Endpoint endpoint1 = new Endpoint("/abc/{id}", "/abc/");
        Endpoint endpoint2 = new Endpoint("/def/{id}", "/def/");
        Assertions.assertFalse(strategy.shouldIgnoreEndpoint(endpoint1));
        Assertions.assertTrue(strategy.shouldIgnoreEndpoint(endpoint2));
    }
}
