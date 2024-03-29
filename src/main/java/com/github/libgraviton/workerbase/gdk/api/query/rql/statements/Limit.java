package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import com.github.libgraviton.workerbase.gdk.api.query.QueryStatement;

/**
 * Limit statement to define the max number of elements that should be returned.
 * Can be used with or without the offset parameter. When not using any offset, it will be handled as 0.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class Limit implements QueryStatement {

    private final int numberOfElements;

    private int offset = 0;

    public Limit(int numberOfElements, int offset) {
        this.numberOfElements = numberOfElements;
        this.offset = offset;
    }

    public Limit(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    @Override
    public String build() {
        return offset > 0 ?
                String.format("limit(%s,%s)", numberOfElements, offset) :
                String.format("limit(%s)", numberOfElements);
    }
}
