package com.github.libgraviton.workerbase.gdk.api.header;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class HeaderTest {


    @Test
    public void testCreateHeader() {
        String headerValue1 = "headerValue1";

        Header header1 = new Header();
        Assertions.assertEquals(0, header1.all().size());

        List<String> headerValues = new ArrayList<>();
        headerValues.add(headerValue1);
        Header header2 = new Header(headerValues);
        Assertions.assertEquals(1, header2.all().size());
        Assertions.assertEquals(headerValue1, header2.get(0));
    }

    @Test
    public void testContains() {
        String headerValue1 = "headerValue1";
        String headerValue2 = "headerValue2";

        List<String> headerValues = new ArrayList<>();
        headerValues.add(headerValue1);
        Header header = new Header(headerValues);
        Assertions.assertTrue(header.contains(headerValue1));
        Assertions.assertFalse(header.contains(headerValue2));
    }
}
