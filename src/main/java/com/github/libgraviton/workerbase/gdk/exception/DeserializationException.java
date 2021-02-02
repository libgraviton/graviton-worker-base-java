package com.github.libgraviton.workerbase.gdk.exception;

import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;

/**
 * Whenever problems occur with deserialization.
 */
public class DeserializationException extends CommunicationException {

    public DeserializationException(String message) {
        super(message);
    }

    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
