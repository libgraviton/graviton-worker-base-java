package com.github.libgraviton.gdk.exception;

/**
 * Whenever the execution of the HTTP request failed.
 */
public class UnsuccessfulRequestException extends CommunicationException {

    public UnsuccessfulRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
