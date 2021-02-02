package com.github.libgraviton.workerbase.gdk.api.query.simple;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleQueryTest {

    @Test
    public void test123() {
        SimpleQuery simpleQuery = new SimpleQuery.Builder()
                .add("param1", "123")
                .add("anotherParam", "value")
                .build();

        String expectedRql = "?anotherParam=value&param1=123";
        assertEquals(expectedRql, simpleQuery.generate());
    }

    @Test
    public void testGenerateWithoutStatements() {
        SimpleQuery simpleQuery = new SimpleQuery.Builder().build();
        String expectedRql = "";
        assertEquals(expectedRql, simpleQuery.generate());
    }
}
