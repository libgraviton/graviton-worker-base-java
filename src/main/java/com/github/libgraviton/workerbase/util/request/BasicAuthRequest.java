package com.github.libgraviton.workerbase.util.request;

import okhttp3.Credentials;
import org.jetbrains.annotations.NotNull;


public class BasicAuthRequest extends RequestBase {

    public BasicAuthRequest(@NotNull String host, @NotNull String route, @NotNull String username, @NotNull String password) {
        super(host, route);
        this.addHeader("Authorization", Credentials.basic(username, password));
    }

    public BasicAuthRequest(@NotNull String host, @NotNull String username, @NotNull String password) {
        this(host, "", username, password);
    }
}