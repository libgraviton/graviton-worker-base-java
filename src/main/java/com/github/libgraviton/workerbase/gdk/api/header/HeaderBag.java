package com.github.libgraviton.workerbase.gdk.api.header;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderBag {

    private final Map<String, Header> headers;

    private HeaderBag(Map<String, Header> headers) {
        this.headers = new HashMap<>(headers);
    }

    public Map<String, Header> all() {
        return new HashMap<>(headers);
    }

    public Header get(String headerName) {
        if (!headers.containsKey(headerName)) {
            headers.put(headerName, new Header());
        }
        return headers.get(headerName);
    }

    public void set(String headerName, String headerValue) {
        headers.put(headerName, new Header(Arrays.asList(headerValue)));
    }

    public String getLink(LinkHeader linkHeader) {
        return getLink(linkHeader.getRel());
    }

    public String getLink(String rel) {
        if (null == headers) {
            return null;
        }

        Header links = this.get("link");
        if (links != null) {
            String linkHeaderSelfPattern = String.format("(?<=<)((?!<).)*(?=>; *rel=\"%s\")", rel);
            for (String link : links) {
                Matcher matcher = Pattern.compile(linkHeaderSelfPattern).matcher(link);
                if (matcher.find()) {
                    return matcher.group(0);
                }
            }
        }
        return null;
    }

    public static class Builder {

        private Map<String, List<String>> headers;

        public Builder() {
            headers = new HashMap<>();
        }

        public Builder(com.github.libgraviton.workerbase.gdk.api.header.HeaderBag headerBag) {
            headers = new HashMap<>();
            if (headerBag != null) {
                for (Map.Entry<String, Header> header : headerBag.all().entrySet()) {
                    headers.put(header.getKey(), header.getValue().all());
                }
            }
        }

        public Builder set(String headerName, List<String> headerValues) {
            headers.put(headerName, headerValues);
            return this;
        }

        public Builder set(String headerName, String headerValue) {
            return set(headerName, headerValue, false);
        }

        public Builder set(String headerName, String headerValue, boolean override) {
            if (!headers.containsKey(headerName) || override) {
                headers.put(headerName, new ArrayList<String>());
            }
            headers.get(headerName).add(headerValue);
            return this;
        }

        public Builder unset(String headerName) {
            if (headers.containsKey(headerName)) {
                headers.remove(headerName);
            }
            return this;
        }

        public Builder unset(String headerName, String headerValue) {
            if (headers.containsKey(headerName) && headers.get(headerName).contains(headerValue)) {
                headers.get(headerName).remove(headerValue);
            }
            return this;
        }

        public com.github.libgraviton.workerbase.gdk.api.header.HeaderBag build() {
            Map<String, Header> builtHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                builtHeaders.put(header.getKey(), new Header(header.getValue()));
            }
            return new com.github.libgraviton.workerbase.gdk.api.header.HeaderBag(builtHeaders);
        }
    }

}
