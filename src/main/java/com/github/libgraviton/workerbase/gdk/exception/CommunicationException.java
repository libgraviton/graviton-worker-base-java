package com.github.libgraviton.workerbase.gdk.exception;

/**
 * Whenever a REST call roundtrip was not successfull (including mapping the response body to a POJO)
 */
public class CommunicationException extends Exception {

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

}
