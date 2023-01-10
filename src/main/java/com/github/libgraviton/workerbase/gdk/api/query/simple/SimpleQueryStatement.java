package com.github.libgraviton.workerbase.gdk.api.query.simple;

import com.github.libgraviton.workerbase.gdk.api.query.QueryStatement;

/**
 * Select statement to define exactly what attributes should be part of the response (as a whitelist).
 * This is especially helpful when querying big elements where only a few attributes are required.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class SimpleQueryStatement implements QueryStatement {

    private final String paramName;

    private final String paramValue;

    public SimpleQueryStatement(String paramName, String paramValue) {
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    @Override
    public String build() {
        return String.format("%s=%s", paramName, paramValue);
    }
}
