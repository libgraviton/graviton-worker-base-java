package com.github.libgraviton.workerbase.gdk.auth;

import com.github.libgraviton.workerbase.gdk.api.header.Header;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class NoAuthTest {

    @Test
    public void testNoAddedHeader() {
        NoAuth auth = new NoAuth();

        HeaderBag.Builder builder = new HeaderBag.Builder();
        auth.addHeader(builder);

        Map<String, Header> headers = builder.build().all();
        Assertions.assertEquals(0, headers.size());
    }
}
