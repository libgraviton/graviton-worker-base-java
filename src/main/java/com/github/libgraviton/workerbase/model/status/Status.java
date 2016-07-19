package com.github.libgraviton.workerbase.model.status;

/**
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public enum Status {
    OPEN("opened"),
    IGNORED("ignored"),
    WORKING("working"),
    DONE("done"),
    FAILED("failed");

    private String value;

    Status(final String value) {
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
