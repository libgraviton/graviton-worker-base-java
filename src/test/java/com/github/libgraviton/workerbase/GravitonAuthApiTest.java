package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.api.header.Header;
import com.github.libgraviton.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.helper.PropertiesLoader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GravitonAuthApiTest {

    @Test
    public void testGetDefaultHeader() throws Exception {
        GravitonAuthApi gravitonApi = new GravitonAuthApi(PropertiesLoader.load());
        HeaderBag headers = gravitonApi.getDefaultHeaders().build();
        assertEquals(3, headers.all().size());

        Header header = headers.get("x-graviton-authentication");
        assertEquals("subnet-java-test", header.get(0));
    }

}
