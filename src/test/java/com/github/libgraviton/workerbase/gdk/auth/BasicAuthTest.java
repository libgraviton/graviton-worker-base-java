package com.github.libgraviton.workerbase.gdk.auth;

import com.github.libgraviton.workerbase.gdk.api.header.Header;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class BasicAuthTest {

    @Test
    public void testEncodedCredentials() {
        String username = "asdf";
        String password = "112233";
        BasicAuth auth = new BasicAuth(username, password);

        String encodedCredentials = auth.basic();
        Assertions.assertEquals("Basic YXNkZjoxMTIyMzM=", encodedCredentials);
    }

    @Test
    public void testAddedHeader() {
        String username = "asdf";
        String password = "112233";
        BasicAuth auth = new BasicAuth(username, password);

        HeaderBag.Builder builder = new HeaderBag.Builder();
        auth.addHeader(builder);

        Map<String, Header> headers = builder.build().all();
        Assertions.assertEquals(1, headers.size());
        Assertions.assertNotNull(headers.get("Authorization"));
    }
}
