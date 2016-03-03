package com.github.libgraviton.workerbase;

/**
 * Exception for unsucessful communication with Graviton
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public class GravitonCommunicationException extends Exception {

    public GravitonCommunicationException() {
    }

    public GravitonCommunicationException(String message) {
        super(message);
    }

    public GravitonCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public GravitonCommunicationException(Throwable cause) {
        super(cause);
    }

    public GravitonCommunicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
