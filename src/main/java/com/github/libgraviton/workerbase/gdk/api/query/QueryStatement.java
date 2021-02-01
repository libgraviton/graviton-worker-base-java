package com.github.libgraviton.workerbase.gdk.api.query;

/**
 * Query statement that represents a single operation.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public interface QueryStatement {

    /**
     * Builds the statement as a String representation.
     *
     * @return generated statement
     */
    String build();
}
