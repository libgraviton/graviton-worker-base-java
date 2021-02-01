package com.github.libgraviton.workerbase.gdk.auth;

import com.github.libgraviton.gdk.api.header.Header;
import com.github.libgraviton.gdk.api.header.HeaderBag;
import com.github.libgraviton.gdk.exception.CommunicationException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BasicAuthTest {

    @Test
    public void testEncodedCredentials() throws CommunicationException {
        String username = "asdf";
        String password = "112233";
        BasicAuth auth = new BasicAuth(username, password);

        String encodedCredentials = auth.basic();
        assertEquals("Basic YXNkZjoxMTIyMzM=", encodedCredentials);
    }

    @Test
    public void testAddedHeader() throws CommunicationException {
        String username = "asdf";
        String password = "112233";
        BasicAuth auth = new BasicAuth(username, password);

        HeaderBag.Builder builder = new HeaderBag.Builder();
        auth.addHeader(builder);

        Map<String, Header> headers = builder.build().all();
        assertEquals(1, headers.size());
        assertNotNull(headers.get("Authorization"));
    }
}
