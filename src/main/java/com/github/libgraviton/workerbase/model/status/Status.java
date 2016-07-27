package com.github.libgraviton.workerbase.model.status;

import java.util.Arrays;
import java.util.List;

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

    private static List<Status> terminatedStates = Arrays.asList(DONE, FAILED, IGNORED);

    private String value;

    Status(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isTerminatedState() {
        return terminatedStates.contains(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
