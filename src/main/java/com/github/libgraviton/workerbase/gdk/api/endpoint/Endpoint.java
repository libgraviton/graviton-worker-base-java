package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.util.PropertiesLoader;

import java.io.Serializable;

/**
 * Represents a Graviton endpoint.
 */
public class Endpoint implements Serializable {

    /**
     * The path of the endpoint to address a single item.
     */
    private String itemPath;

    /**
     * The path of the endpoint.
     */
    private String path;


    /**
     * Loads the base url once from the properties the first time an Endpoint class will be used.
     */
    private static transient String baseUrl = PropertiesLoader.load("graviton.base.url");

    /**
     * Constructor. Sets the endpoint itemUrl.
     *
     * @param itemPath The endpoint itemUrl.
     */
    public Endpoint(String itemPath) {
        this.itemPath = itemPath;
    }

    /**
     * Constructor. Sets the item and collection endpoint itemUrl.
     *
     * @param itemPath The item endpoint path.
     * @param path The endpoint path.
     */
    public Endpoint(String itemPath, String path) {
        this.itemPath = itemPath;
        this.path = path;
    }

    public String getUrl() {
        return path != null ? baseUrl + path : path;
    }

    public String getItemUrl() {
        return itemPath != null ? baseUrl + itemPath : itemPath;
    }

    public String getItemPath() {
        return itemPath;
    }

    public String getPath() {
        return path;
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint)) {
            return false;
        }
        com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint endpoint = (com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint) obj;
        return ((null == itemPath && null == endpoint.itemPath) || (null != itemPath && itemPath.equals(endpoint.itemPath))) &&
                ((null == path && null == endpoint.path) ||
                    (null != path && path.equals(endpoint.path)));
    }
}
