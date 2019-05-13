package com.github.libgraviton.gdk.auth;

import com.github.libgraviton.gdk.api.header.HeaderBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoAuth implements HeaderAuth {

    private static final Logger LOG = LoggerFactory.getLogger(NoAuth.class);

    @Override
    public void addHeader(HeaderBag.Builder headerBuilder) {
        LOG.debug("No auth selected.");
    }
}
