package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LimitTest {

    @Test
    public void testBuildWithOffset() {
        Limit limit = new Limit(1, 2);
        Assertions.assertEquals("limit(1,2)", limit.build());
    }

    @Test
    public void testBuildWithoutOffset() {
        Limit limit = new Limit(1);
        Assertions.assertEquals("limit(1)", limit.build());
    }
}
