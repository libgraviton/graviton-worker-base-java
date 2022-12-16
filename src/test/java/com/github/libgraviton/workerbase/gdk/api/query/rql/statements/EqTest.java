package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EqTest {

    @Test
    public void testBuild() {
        Eq eq = new Eq("name1", "value1");
        Assertions.assertEquals("eq(name1,value1)", eq.build());
    }

    @Test
    public void testBuildNullValues() {
        Eq eq = new Eq(null, null);
        Assertions.assertEquals("eq(null,null)", eq.build());
    }
}
