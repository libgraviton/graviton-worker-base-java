package com.github.libgraviton.workerbase.gdk.auth;

import com.github.libgraviton.workerbase.gdk.api.header.Header;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NoAuthTest {

    @Test
    public void testNoAddedHeader() throws CommunicationException {
        NoAuth auth = new NoAuth();

        HeaderBag.Builder builder = new HeaderBag.Builder();
        auth.addHeader(builder);

        Map<String, Header> headers = builder.build().all();
        assertEquals(0, headers.size());
    }
}
