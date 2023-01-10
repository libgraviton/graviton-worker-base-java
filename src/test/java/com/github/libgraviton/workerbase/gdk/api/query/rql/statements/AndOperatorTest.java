package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import com.github.libgraviton.workerbase.gdk.api.query.QueryStatement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AndOperatorTest {

    @Test
    public void testBuild() {
        List<QueryStatement> statements = new ArrayList<>();
        QueryStatement statement1 = mock(QueryStatement.class);
        when(statement1.build()).thenReturn("asdf");
        QueryStatement statement2 = mock(QueryStatement.class);
        when(statement2.build()).thenReturn("fdsa");
        statements.add(statement1);
        statements.add(statement2);

        AndOperator andOperator = new AndOperator();
        andOperator.addStatements(statements);
        Assertions.assertEquals("and(asdf,fdsa)", andOperator.build());
    }



    @Test
    public void testBuildWithException() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            AndOperator andOperator = new AndOperator();
            andOperator.build();
        });
    }
}
