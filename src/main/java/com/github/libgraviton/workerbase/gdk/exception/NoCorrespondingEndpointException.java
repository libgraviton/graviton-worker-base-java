package com.github.libgraviton.workerbase.gdk.exception;

public class NoCorrespondingEndpointException extends IllegalArgumentException {

    public NoCorrespondingEndpointException(String className) {
        super("Could not find an endpoint corresponding to POJO class '" + className + "'.");
    }

}
