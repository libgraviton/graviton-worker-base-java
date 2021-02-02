package com.github.libgraviton.workerbase.gdk.api.header;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HeaderTest {


    @Test
    public void testCreateHeader() {
        String headerValue1 = "headerValue1";

        Header header1 = new Header();
        assertEquals(0, header1.all().size());

        List<String> headerValues = new ArrayList<>();
        headerValues.add(headerValue1);
        Header header2 = new Header(headerValues);
        assertEquals(1, header2.all().size());
        assertEquals(headerValue1, header2.get(0));
    }

    @Test
    public void testContains() {
        String headerValue1 = "headerValue1";
        String headerValue2 = "headerValue2";

        List<String> headerValues = new ArrayList<>();
        headerValues.add(headerValue1);
        Header header = new Header(headerValues);
        assertTrue(header.contains(headerValue1));
        assertFalse(header.contains(headerValue2));
    }
}
