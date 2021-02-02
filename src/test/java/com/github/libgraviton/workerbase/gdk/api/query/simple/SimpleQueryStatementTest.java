package com.github.libgraviton.workerbase.gdk.api.query.simple;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleQueryStatementTest {

    @Test
    public void testBuild() {
        SimpleQueryStatement simpleQueryStatement = new SimpleQueryStatement("paramName1", "paramValue1");
        assertEquals("paramName1=paramValue1", simpleQueryStatement.build());
    }
}
