package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.api.endpoint.Blacklist;
import com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint;
import com.github.libgraviton.workerbase.gdk.api.endpoint.Whitelist;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Strategy handler to define what endpoints should be ignored or included.
 */
public class EndpointInclusionStrategy {

    public enum Strategy {
        WHITELIST, BLACKLIST, DEFAULT
    }

    protected Set<String> endpointPaths;

    public EndpointInclusionStrategy(String path) {
        if (path == null) {
            endpointPaths = new HashSet<>();
            return;
        }

        try {
            List<String> endpoints = FileUtils.readLines(new File(path), Charset.defaultCharset());
            endpointPaths = new HashSet<>(endpoints);
        } catch (IOException e) {
            endpointPaths = new HashSet<>();
        }
    }

    public static com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointInclusionStrategy create(Strategy selectedStrategy, String path) {
        com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointInclusionStrategy strategy;

        if (selectedStrategy.equals(Strategy.WHITELIST)) {
            strategy = new Whitelist(path);
        } else if (selectedStrategy.equals(Strategy.BLACKLIST)) {
            strategy = new Blacklist(path);
        } else {
            // allows all endpoints
            strategy = new com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointInclusionStrategy(path);
        }

        return strategy;
    }

    public boolean shouldIgnoreEndpoint(Endpoint endpoint) {
        return false;
    }
}
