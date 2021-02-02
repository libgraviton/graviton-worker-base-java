package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SelectTest {

    @Test
    public void testBuild() {
        Select select = new Select();
        select.add("attributeName1");
        select.add("attributeName2");
        assertEquals("select(attributeName1,attributeName2)", select.build());

        List<String> attributeNames = new ArrayList<>();
        attributeNames.add("attributeName3");
        attributeNames.add("attributeName4");
        select.add(attributeNames);
        assertEquals("select(attributeName1,attributeName2,attributeName3,attributeName4)", select.build());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildWithException() {
        Select select = new Select();
        select.build();
    }
}
