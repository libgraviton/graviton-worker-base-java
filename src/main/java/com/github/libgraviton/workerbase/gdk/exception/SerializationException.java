package com.github.libgraviton.workerbase.gdk.exception;

import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;

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
