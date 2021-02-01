package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint;
import com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointInclusionStrategy;

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
