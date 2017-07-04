package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.api.header.Header;
import com.github.libgraviton.gdk.api.header.HeaderBag;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GravitonAuthApiTest {

    @Test
    public void testGetDefaultHeader() {
        GravitonAuthApi gravitonApi = new GravitonAuthApi();
        HeaderBag headers = gravitonApi.getDefaultHeaders();
        assertEquals(3, headers.all().size());

        Header header = headers.get("x-graviton-authentication");
        assertEquals("subnet-java-test", header.get(0));
    }

}
