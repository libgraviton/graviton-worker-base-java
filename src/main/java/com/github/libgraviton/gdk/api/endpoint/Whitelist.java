package com.github.libgraviton.gdk.api.endpoint;

/**
 * Endpoint strategy to include all endpoints on the whitelist
 */
public class Whitelist extends EndpointInclusionStrategy {

    public Whitelist(String path) {
        super(path);
    }

    @Override
    public boolean shouldIgnoreEndpoint(Endpoint endpoint) {
        return !endpointPaths.contains(endpoint.getPath());
    }
}
