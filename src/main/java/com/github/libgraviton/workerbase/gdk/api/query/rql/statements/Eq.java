package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import com.github.libgraviton.workerbase.gdk.api.query.QueryStatement;

/**
 * Equals statement that contains of a name and a value.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class Eq implements QueryStatement {

    private String name;

    private String value;

    public Eq(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String build() {
        return "eq(" + name + "," + value + ")";
    }
}
