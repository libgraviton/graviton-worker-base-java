package com.github.libgraviton.workerbase.gdk.api.header;

public enum LinkHeader {

    SELF ("self"),
    EVENT_STATUS ("eventStatus");

    private String rel;

    LinkHeader(String rel) {
        this.rel = rel;
    }

    public String getRel() {
        return rel;
    }
}
