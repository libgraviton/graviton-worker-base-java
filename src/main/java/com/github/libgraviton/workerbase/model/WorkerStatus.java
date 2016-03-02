package com.github.libgraviton.workerbase.model;

/**
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public enum WorkerStatus {
    OPEN("opened"),
    WORKING("working"),
    DONE("done"),
    FAILED("failed");

    private String value;

    WorkerStatus(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
