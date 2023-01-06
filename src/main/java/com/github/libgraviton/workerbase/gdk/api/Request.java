package com.github.libgraviton.workerbase.gdk.api;

import com.github.libgraviton.workerbase.gdk.RequestExecutor;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.multipart.FilePart;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.api.query.Query;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulRequestException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import io.activej.inject.annotation.Inject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {

    protected URL url;

    protected HttpMethod method;

    protected HeaderBag headers;

    protected byte[] body;

    protected List<Part> parts;

    protected List<FilePart> fileParts;

    protected Map<String, String> params = new HashMap<>();

    protected Request() {
    }

    protected Request(Builder builder) throws MalformedURLException {
        method = builder.method;
        url = builder.buildUrl();
        headers = builder.headerBuilder.build();
        body = builder.body;
        parts = builder.parts;
        fileParts = builder.fileParts;
        params = builder.params;
    }

    public URL getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public HeaderBag getHeaders() {
        return headers;
    }

    public byte[] getBodyBytes() {
        return body;
    }

    public String getBody() {
        return body != null ? new String(body) : null;
    }

    public List<Part> getParts() {
        return parts;
    }

    public List<FilePart> getFileParts() {
        return fileParts;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public boolean isMultipartRequest() {
        return (parts.size() + fileParts.size()) > 0;
    }

    public static class Builder {

        protected String url;

        protected Map<String, String> params = new HashMap<>();

        protected HttpMethod method = HttpMethod.GET;

        protected HeaderBag.Builder headerBuilder = new HeaderBag.Builder();

        protected Query query;

        protected byte[] body;

        protected List<Part> parts = new ArrayList<>();

        protected List<FilePart> fileParts = new ArrayList<>();

        protected final RequestExecutor executor;

        public Builder() {
            this.executor = DependencyInjection.getInstance(RequestExecutor.class);
        }

        public Builder setUrl(URL url) {
            return setUrl(url.toExternalForm());
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Builder addParam(String paramName, String paramValue) {
            params.put(paramName, paramValue);
            return this;
        }

        public Builder setParams(Map<String, String> params) {
            this.params = new HashMap<>(params);
            return this;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public Builder setMethod(HttpMethod method) {
            this.method = method;
            return this;
        }

        public HttpMethod getMethod() {
            return method;
        }

        public Builder addHeader(String headerName, String headerValue) {
            return addHeader(headerName, headerValue, false);
        }

        public Builder addHeader(String headerName, String headerValue, boolean override) {
            headerBuilder.set(headerName, headerValue, override);
            return this;
        }

        public Builder setHeaders(HeaderBag headerBag) {
            headerBuilder = new HeaderBag.Builder(headerBag);
            return this;
        }

        public HeaderBag.Builder getHeaders() {
            return headerBuilder;
        }

        public Builder setQuery(Query query) {
            if(this.query == null) {
                this.query = query;
            } else {
                this.query.addStatements(query.getStatements());
            }

            return this;
        }

        public Builder setBody(String body) {
            if (body != null) {
                this.body = body.getBytes();
            } else {
                this.body = null;
            }
            return this;
        }

        public Builder setBody(byte[] body) {
            this.body = body;
            return this;
        }

        /**
         * return the body.
         *
         * @return body
         */
        public String getBody() {
            return body == null ? null : new String(body);
        }

        public Builder setParts(List<Part> parts) {
            this.parts = parts;
            return this;
        }

        public Builder addPart(Part part) {
            this.parts.add(part);
            return this;
        }

        public List<Part> getParts() {
            return parts;
        }

        public Builder setFileParts(List<FilePart> parts) {
            this.fileParts = parts;
            return this;
        }

        public Builder addFilePart(FilePart part) {
            this.fileParts.add(part);
            return this;
        }

        public List<FilePart> getFileParts() {
            return fileParts;
        }

        public Builder head() {
            return setMethod(HttpMethod.HEAD);
        }

        public Builder options() {
            return setMethod(HttpMethod.OPTIONS);
        }

        public Builder get() {
            return setMethod(HttpMethod.GET);
        }

        public Builder delete() {
            return setMethod(HttpMethod.DELETE);
        }

        public Builder post(String data) {
            return setMethod(HttpMethod.POST).setBody(data);
        }

        // Multipart POST request
        public Builder post(Part... parts) {
            for (Part part : parts) {
                addPart(part);
            }
            return setMethod(HttpMethod.POST);
        }

        public Builder put(String data) {
            return setMethod(HttpMethod.PUT).setBody(data);
        }

        // Multipart PUT request
        public Builder put(Part... parts) {
            for (Part part : parts) {
                addPart(part);
            }
            return setMethod(HttpMethod.PUT);
        }

        public Builder patch(String data) {
            return setMethod(HttpMethod.PATCH).setBody(data);
        }

        public com.github.libgraviton.workerbase.gdk.api.Request build() throws MalformedURLException {
            return new com.github.libgraviton.workerbase.gdk.api.Request(this);
        }

        public Response execute() throws CommunicationException {
            try {
                return executor.execute(build());
            } catch (MalformedURLException e) {
                throw new UnsuccessfulRequestException(String.format("'%s' to '%s' failed due to malformed url.", method, url),
                        e);
            }
        }

        protected URL buildUrl() throws MalformedURLException {
            String generatedQuery = query != null ? query.generate() : "";
            String url = this.url.concat(generatedQuery);
            for (Map.Entry<String, String> param : params.entrySet()) {
                url = url.replace(String.format("{%s}", param.getKey()), param.getValue());
            }

            return new URL(url);
        }
    }
}
