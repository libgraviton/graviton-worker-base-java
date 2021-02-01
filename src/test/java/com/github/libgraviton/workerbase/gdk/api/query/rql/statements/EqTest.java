package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EqTest {

    @Test
    public void testBuild() {
        Eq eq = new Eq("name1", "value1");
        assertEquals("eq(name1,value1)", eq.build());
    }

    @Test
    public void testBuildNullValues() {
        Eq eq = new Eq(null, null);
        assertEquals("eq(null,null)", eq.build());
    }
}
