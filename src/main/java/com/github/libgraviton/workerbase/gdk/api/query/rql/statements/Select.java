package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import com.github.libgraviton.workerbase.gdk.api.query.QueryStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Select statement to define exactly what attributes should be part of the response (as a whitelist).
 * This is especially helpful when querying big elements where only a few attributes are required.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class Select implements QueryStatement {

    private List<String> attributeNames = new ArrayList<>();

    public void add(String attributeName) {
        this.attributeNames.add(attributeName);
    }

    public void add(List<String> attributeNames) {
        this.attributeNames.addAll(attributeNames);
    }

    @Override
    public String build() {
        if (attributeNames == null || attributeNames.isEmpty()) {
            throw new IllegalStateException("Select statement requires at least 1 element");
        }
        StringJoiner joiner = new StringJoiner(",");
        attributeNames.forEach(joiner::add);
        return String.format("select(%s)", joiner);
    }
}
