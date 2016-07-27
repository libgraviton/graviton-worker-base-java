package com.github.libgraviton.workerbase.exception;

/**
 * Exception represents an unsuccessful HTTP response.
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public class UnsuccessfulHttpResponseException extends Exception {
    public UnsuccessfulHttpResponseException() {
    }

    public UnsuccessfulHttpResponseException(String message) {
        super(message);
    }

    public UnsuccessfulHttpResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsuccessfulHttpResponseException(Throwable cause) {
        super(cause);
    }

    public UnsuccessfulHttpResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
