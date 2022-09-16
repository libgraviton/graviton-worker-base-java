package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.helper.WorkerProperties;

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
     * Constructor. Sets the endpoint itemUrl.
     *
     * @param itemPath The endpoint itemUrl.
     */
    public Endpoint(String itemPath) {
        setData(itemPath, null);
    }

    /**
     * Constructor. Sets the item and collection endpoint itemUrl.
     *
     * @param itemPath The item endpoint path.
     * @param path The endpoint path.
     */
    public Endpoint(String itemPath, String path) {
        setData(itemPath, path);
    }

    protected void setData(String itemPath, String path) {
        this.itemPath = itemPath;
        this.path = path;
    }

    public String getUrl() {
        return path != null ? getBaseUrl() + path : path;
    }

    public String getItemUrl() {
        return itemPath != null ? getBaseUrl() + itemPath : itemPath;
    }

    public String getItemPath() {
        return itemPath;
    }

    public String getPath() {
        return path;
    }

    public static String getBaseUrl() {
        return WorkerProperties.getProperty("graviton.base.url");
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof Endpoint)) {
            return false;
        }
        Endpoint endpoint = (Endpoint) obj;
        return ((null == itemPath && null == endpoint.itemPath) || (null != itemPath && itemPath.equals(endpoint.itemPath))) &&
                ((null == path && null == endpoint.path) ||
                    (null != path && path.equals(endpoint.path)));
    }
}
