package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.exception.NoCorrespondingEndpointException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Manages all available endpoints of and associates them with a corresponding POJO class.
 */
public class EndpointManager {

    /**
     * The POJO class to endpoint association.
     */
    protected Map<String, Endpoint> endpoints = new HashMap<>();

    protected EndpointInclusionStrategy strategy;

    /**
     * Adds a endpoint and associates it with a given POJO class.
     *
     * @param className The POJO class name.
     * @param endpoint The endpoint.
     *
     * @return The number of currently added endpoints.
     */
    public int addEndpoint(String className, Endpoint endpoint) {
        endpoints.put(className, endpoint);
        return endpoints.size();
    }

    /**
     * Tells whether the service manager is aware of an endpoint for a given POJO class.
     *
     * @param className The POJO class name.
     *
     * @return true when the service manager is aware of a service of the given POJO class, otherwise false.
     */
    public boolean hasEndpoint(String className) {
        return endpoints.containsKey(className);
    }

    /**
     * Gets the endpoint associated to the given class.
     *
     * @param className The class name.
     *
     * @return The associated service.
     */
    public Endpoint getEndpoint(String className) {
        if (!hasEndpoint(className)) {
            throw new NoCorrespondingEndpointException(className);
        }
        return endpoints.get(className);
    }

    /**
     * Finds endpoints that end in a certain suffix (so you can search by the simplified classname)
     *
     * @param classNameSuffix just class name
     * @return the endpoint
     */
    public Map<String, Endpoint> findEndpoints(String classNameSuffix) {
        Map<String, Endpoint> results = new HashMap<>();
        for (Entry<String, Endpoint> endpoint : endpoints.entrySet()) {
            if (endpoint.getKey().endsWith(classNameSuffix)) {
                results.put(endpoint.getKey(), endpoint.getValue());
            }
        }
        return results;
    }

    /**
     * Find a single endpoint that matches a certain suffix
     *
     * @param classNameSuffix just class name
     * @return the endpoint
     */
    public Endpoint findEndpoint(String classNameSuffix) {
        Map<String, Endpoint> results = findEndpoints(classNameSuffix);
        if (!results.isEmpty()) {
            return results.entrySet().iterator().next().getValue();
        }
        return null;
    }

    /**
     * Find a single endpoint by items itemPath
     *
     * @param itemPath item path
     * @return the endpoint
     */
    public Entry<String, Endpoint> findEndpointByItemPath(String itemPath) {

        // searcher
        String[] paths = itemPath.split("/");
        paths[paths.length-1] = "{id}";

        String searcher = Arrays.stream(paths)
            .collect(Collectors.joining("/"));

        for (Entry<String, Endpoint> endpointItem : endpoints.entrySet()) {
            if (searcher.equals(endpointItem.getValue().getItemPath())) {
                return endpointItem;
            }
        }

        return null;
    }

    /**
     * Determines if the endpoint should be ignore.
     * @param endpoint endpoint to check
     * @return true if the endpoint should be ignored
     */
    public boolean shouldIgnoreEndpoint(Endpoint endpoint) {
        return strategy.shouldIgnoreEndpoint(endpoint);
    }

    public void setEndpointInclusionStrategy(EndpointInclusionStrategy strategy) {
        this.strategy = strategy;
    }
}
