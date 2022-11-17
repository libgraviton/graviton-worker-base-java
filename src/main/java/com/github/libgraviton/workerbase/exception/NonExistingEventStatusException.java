package com.github.libgraviton.workerbase.exception;

/**
 * Shows that a given event status could not been retrieved from graviton
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public class NonExistingEventStatusException extends GravitonCommunicationException {
    public NonExistingEventStatusException(String message) {
        super(message);
    }
}
