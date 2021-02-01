package com.github.libgraviton.workerbase.gdk.auth;

import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;

public interface HeaderAuth {

    void addHeader(HeaderBag.Builder headerBuilder);
}
