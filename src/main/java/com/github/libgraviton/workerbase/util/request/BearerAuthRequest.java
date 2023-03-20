package com.github.libgraviton.workerbase.util.request;

import org.jetbrains.annotations.NotNull;


public class BearerAuthRequest extends RequestBase {

    public BearerAuthRequest(@NotNull String host, @NotNull String route, @NotNull String bearer) {
        super(host, route);
        this.addHeader("Authorization", "Bearer " + bearer);
    }

    public BearerAuthRequest(@NotNull String host, @NotNull String bearer) {
        this(host,"", bearer);
    }
}