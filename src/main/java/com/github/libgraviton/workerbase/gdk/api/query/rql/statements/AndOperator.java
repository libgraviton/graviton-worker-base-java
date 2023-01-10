package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import com.github.libgraviton.workerbase.gdk.api.query.QueryStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * AND connection between at least two RQL statements.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class AndOperator implements QueryStatement {

    private List<QueryStatement> statements;

    public void addStatement(QueryStatement statement) {
        if (statements == null) {
            statements = new ArrayList<>();
        }

        statements.add(statement);
    }

    public void addStatements(List<QueryStatement> statements) {
        statements.forEach(this::addStatement);
    }

    @Override
    public String build() {
        if (statements == null || statements.size() < 2) {
            throw new IllegalStateException("AND operator requires at least 2 elements");
        }
        StringJoiner joiner = new StringJoiner(",");
        statements.forEach(statement -> joiner.add(statement.build()));
        return String.format("and(%s)", joiner);
    }
}
