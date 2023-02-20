package com.github.libgraviton.workerbase.gdk.api.endpoint;

/**
 * Endpoint strategy to ignore all endpoints on the blacklist
 */
public class Blacklist extends EndpointInclusionStrategy {

    public Blacklist(String path) {
        super(path);
    }

    @Override
    public boolean shouldIgnoreEndpoint(Endpoint endpoint) {
        return endpointPaths.contains(endpoint.getPath()) || endpointPaths.contains(endpoint.getItemPath());
    }
}
