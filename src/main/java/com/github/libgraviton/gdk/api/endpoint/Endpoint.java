package com.github.libgraviton.gdk.api.endpoint;

/**
 * here for BC compat
 */
public class Endpoint extends com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint {

    private static final long serialVersionUID = 7449263148840626112L;

    public Endpoint(String itemPath) {
        super(itemPath);
    }

    public Endpoint(String itemPath, String path) {
        super(itemPath, path);
    }
}
