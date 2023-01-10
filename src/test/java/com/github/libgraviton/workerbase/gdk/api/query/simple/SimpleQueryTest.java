package com.github.libgraviton.workerbase.gdk.api.query.simple;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleQueryTest {

    @Test
    public void test123() {
        SimpleQuery simpleQuery = new SimpleQuery.Builder()
                .add("param1", "123")
                .add("anotherParam", "value")
                .build();

        String expectedRql = "?anotherParam=value&param1=123";
        Assertions.assertEquals(expectedRql, simpleQuery.generate());
    }

    @Test
    public void testGenerateWithoutStatements() {
        SimpleQuery simpleQuery = new SimpleQuery.Builder().build();
        String expectedRql = "";
        Assertions.assertEquals(expectedRql, simpleQuery.generate());
    }
}
