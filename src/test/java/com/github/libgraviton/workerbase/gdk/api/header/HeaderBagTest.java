package com.github.libgraviton.workerbase.gdk.api.header;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class HeaderBagTest {

    @Test
    public void testLink() {
        String inputLink = "<http://localhost:8000/some/graviton/endpoint/1234>; rel=\"self\",<http://localhost:8000/event/status/20c3b1f9c3b83d339bd88e8e5b0d7066>; rel=\"eventStatus\"";
        String expectedLink = "http://localhost:8000/event/status/20c3b1f9c3b83d339bd88e8e5b0d7066";
        HeaderBag headers = new HeaderBag.Builder()
                .set("link", inputLink)
                .build();

        assertEquals(expectedLink, headers.getLink(LinkHeader.EVENT_STATUS));

        headers = new HeaderBag.Builder()
                .build();

        assertNull(expectedLink, headers.getLink(LinkHeader.EVENT_STATUS));
    }

    @Test
    public void testSetNoHeader() {
        HeaderBag headers = new HeaderBag.Builder()
                .build();
        assertEquals(0, headers.all().size());
    }

    @Test
    public void testSetOneHeader() {
        String headerName = "headerName";
        String headerValue1 = "headerValue";
        HeaderBag headers = new HeaderBag.Builder()
                .set(headerName, headerValue1)
                .build();
        assertEquals(1, headers.all().size());
        assertEquals(1, headers.get(headerName).all().size());
        assertEquals(headerValue1, headers.get(headerName).get(0));
    }

    @Test
    public void testSetTwoHeaders() {
        String headerName = "headerName";
        String headerValue1 = "headerValue";
        String headerValue2 = "headerValue";
        HeaderBag headers = new HeaderBag.Builder()
                .set(headerName, headerValue1)
                .set(headerName, headerValue2)
                .build();
        assertEquals(1, headers.all().size());
        assertEquals(2, headers.get(headerName).all().size());
        assertEquals(headerValue1, headers.get(headerName).get(0));
        assertEquals(headerValue2, headers.get(headerName).get(1));
    }

    @Test
    public void testSetTwoHeadersWithOverride() {
        String headerName = "headerName";
        String headerValue1 = "headerValue";
        String headerValue2 = "headerValue";
        HeaderBag headers = new HeaderBag.Builder()
                .set(headerName, headerValue1)
                .set(headerName, headerValue2, true)
                .build();
        assertEquals(1, headers.all().size());
        assertEquals(1, headers.get(headerName).all().size());
        assertEquals(headerValue2, headers.get(headerName).get(0));
    }

    @Test
    public void testAllSetHeadersAtOnce() {
        String headerName = "headerName";
        String headerValue1 = "headerValue";
        String headerValue2 = "headerValue";
        HeaderBag headers = new HeaderBag.Builder()
                .set(headerName, Arrays.asList(headerValue1, headerValue2))
                .build();
        assertEquals(1, headers.all().size());
        assertEquals(2, headers.get(headerName).all().size());
        assertEquals(headerValue1, headers.get(headerName).get(0));
        assertEquals(headerValue2, headers.get(headerName).get(1));
    }

    @Test
    public void testUnsetHeaders() {
        String headerName1 = "headerName1";
        String headerName2 = "headerName2";
        String headerValue1 = "headerValue1";
        String headerValue2 = "headerValue2";
        HeaderBag headers = new HeaderBag.Builder()
                .set(headerName1, headerValue1)
                .set(headerName2, headerValue2)
                .unset(headerName1)
                .unset(headerName2, headerValue2)
                .build();
        assertEquals(1, headers.all().size());
    }

    @Test
    public void testBuilder() {
        String headerName = "headerName";
        String headerValue = "headerValue";
        HeaderBag headers1 = new HeaderBag.Builder()
                .set(headerName, headerValue)
                .build();
        assertEquals(1, headers1.all().size());
        assertEquals(headerValue, headers1.get(headerName).get(0));
        HeaderBag headers2 = new HeaderBag.Builder(headers1)
                .build();
        assertNotEquals(headers1, headers2);
        assertEquals(1, headers1.all().size());
        assertEquals(headerValue, headers1.get(headerName).get(0));
        HeaderBag headers3 = new HeaderBag.Builder(null)
                .build();
        assertEquals(0, headers3.all().size());
    }
}
