package com.github.libgraviton.gdk.api.endpoint;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * here for BC compat
 */
public class Endpoint extends com.github.libgraviton.workerbase.gdk.api.endpoint.Endpoint {

    private static final long serialVersionUID = 7449263148840626112L;

    /**
     * The path of the endpoint to address a single item.
     */
    private String itemPath;

    /**
     * The path of the endpoint.
     */
    private String path;

    public Endpoint(String itemPath) {
        super(itemPath);
    }

    public Endpoint(String itemPath, String path) {
        super(itemPath, path);
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        setData(itemPath, path);
    }
}
