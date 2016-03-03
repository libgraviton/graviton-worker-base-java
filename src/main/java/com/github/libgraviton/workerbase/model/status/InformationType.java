package com.github.libgraviton.workerbase.model.status;

/**
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public enum InformationType {
    DEBUG("debug"),
    INFO("info"),
    WARNING("warning"),
    ERROR("error");

    private String value;

    InformationType(final String value) {
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
