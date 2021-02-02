package com.github.libgraviton.workerbase.gdk.api.endpoint.exception;

public class UnableToPersistEndpointAssociationsException extends Exception {

    public UnableToPersistEndpointAssociationsException(String message) {
        super(message);
    }

    public UnableToPersistEndpointAssociationsException(String message, Exception e) {
        super(message, e);
    }

}
