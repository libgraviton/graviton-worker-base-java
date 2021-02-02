package com.github.libgraviton.workerbase.gdk.api.query.simple;

import com.github.libgraviton.workerbase.gdk.api.query.Query;
import com.github.libgraviton.workerbase.gdk.api.query.QueryStatement;
import com.github.libgraviton.workerbase.gdk.api.query.simple.SimpleQueryStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Url query handler where every query param name can be used once.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class SimpleQuery extends Query {

    private SimpleQuery(List<QueryStatement> statements) {
        this.statements = statements;
    }

    public static class Builder {
        Map<String, String> params = new HashMap<>();

        public Builder add(String paramName, String paramValue) {
            params.put(paramName, paramValue);
            return this;
        }

        public com.github.libgraviton.workerbase.gdk.api.query.simple.SimpleQuery build() {
            List<QueryStatement> statements = new ArrayList<>();
            params.forEach((name, value) -> statements.add(new SimpleQueryStatement(name, value)));

            return new com.github.libgraviton.workerbase.gdk.api.query.simple.SimpleQuery(statements);
        }
    }
}