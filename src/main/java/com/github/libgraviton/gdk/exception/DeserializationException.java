package com.github.libgraviton.gdk.exception;

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
