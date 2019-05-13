package com.github.libgraviton.gdk.api.endpoint.exception;

public class UnableToLoadEndpointAssociationsException extends Exception {

    public UnableToLoadEndpointAssociationsException(String message) {
        super(message);
    }

    public UnableToLoadEndpointAssociationsException(String message, Exception e) {
        super(message, e);
    }

}
