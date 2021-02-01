package com.github.libgraviton.workerbase.gdk.auth;

import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.auth.HeaderAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoAuth implements HeaderAuth {

    private static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.gdk.auth.NoAuth.class);

    @Override
    public void addHeader(HeaderBag.Builder headerBuilder) {
        LOG.debug("No auth selected.");
    }
}
