package com.github.libgraviton.workerbase.gdk.api.query.simple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleQueryStatementTest {

    @Test
    public void testBuild() {
        SimpleQueryStatement simpleQueryStatement = new SimpleQueryStatement("paramName1", "paramValue1");
        Assertions.assertEquals("paramName1=paramValue1", simpleQueryStatement.build());
    }
}
