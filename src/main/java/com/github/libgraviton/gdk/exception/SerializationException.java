package com.github.libgraviton.gdk.exception;

/**
 * Whenever problems occur with serialization.
 */
public class SerializationException extends CommunicationException {

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
