package com.github.libgraviton.gdk.auth;

import com.github.libgraviton.gdk.api.header.HeaderBag;

public interface HeaderAuth {

    void addHeader(HeaderBag.Builder headerBuilder);
}
