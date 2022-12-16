package com.github.libgraviton.workerbase.gdk.api.query.rql.statements;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SelectTest {

    @Test
    public void testBuild() {
        Select select = new Select();
        select.add("attributeName1");
        select.add("attributeName2");
        Assertions.assertEquals("select(attributeName1,attributeName2)", select.build());

        List<String> attributeNames = new ArrayList<>();
        attributeNames.add("attributeName3");
        attributeNames.add("attributeName4");
        select.add(attributeNames);
        Assertions.assertEquals("select(attributeName1,attributeName2,attributeName3,attributeName4)", select.build());
    }

    @Test
    public void testBuildWithException() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            Select select = new Select();
            select.build();
        });
    }
}
