package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint;
import com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointInclusionStrategy;

/**
 * Endpoint strategy to ignore all endpoints on the blacklist
 */
public class Blacklist extends EndpointInclusionStrategy {

    public Blacklist(String path) {
        super(path);
    }

    @Override
    public boolean shouldIgnoreEndpoint(Endpoint endpoint) {
        return endpointPaths.contains(endpoint.getPath());
    }
}
