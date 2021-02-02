package com.github.libgraviton.workerbase.gdk.exception;

import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;

/**
 * Whenever the execution of the HTTP request failed.
 */
public class UnsuccessfulRequestException extends CommunicationException {

    public UnsuccessfulRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
