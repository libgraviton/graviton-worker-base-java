package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LimitTest {

    @Test
    public void testBuildWithOffset() {
        Limit limit = new Limit(1, 2);
        assertEquals("limit(1,2)", limit.build());
    }

    @Test
    public void testBuildWithoutOffset() {
        Limit limit = new Limit(1);
        assertEquals("limit(1)", limit.build());
    }
}
