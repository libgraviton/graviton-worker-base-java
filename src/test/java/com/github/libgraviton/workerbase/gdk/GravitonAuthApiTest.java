package com.github.libgraviton.workerbase.gdk;

import com.github.libgraviton.workerbase.gdk.api.header.Header;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GravitonAuthApiTest {

    @Test
    public void testGetDefaultHeader() {
        GravitonApi gravitonApi = DependencyInjection.getInstance(GravitonApi.class);
        HeaderBag headers = gravitonApi.getDefaultHeaders().build();
        assertEquals(3, headers.all().size());

        Header header = headers.get("x-graviton-authentication");
        assertEquals("subnet-java-test", header.get(0));
    }

}
