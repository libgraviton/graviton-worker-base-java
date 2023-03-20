package com.github.libgraviton.workerbase.util.request;

import org.jetbrains.annotations.NotNull;


public class NoAuthRequest extends RequestBase {
    public NoAuthRequest(@NotNull String host) {
        super(host);
    }
}
