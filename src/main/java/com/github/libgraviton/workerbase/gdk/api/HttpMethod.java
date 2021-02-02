package com.github.libgraviton.workerbase.gdk.api;

public enum HttpMethod {

    OPTIONS ("OPTIONS"),
    HEAD ("HEAD"),
    GET ("GET"),
    POST ("POST"),
    PUT ("PUT"),
    PATCH ("PATCH"),
    DELETE ("DELETE");

    private String method;

    HttpMethod(String method) {
        this.method = method;
    }

    public String asString() {
        return method;
    }
}
