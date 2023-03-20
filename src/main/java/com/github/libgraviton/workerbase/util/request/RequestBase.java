package com.github.libgraviton.workerbase.util.request;

import com.github.libgraviton.workerbase.util.General;
import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulResponseException;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;


public class RequestBase {

    private final Request.Builder builder = new Request.Builder();
    private final String host;
    private String route = "";
    private String body;
    private HttpMethod httpMethod;
    private HashMap<String, String> headers = new HashMap<>();
    private HashMap<String, String> params = new HashMap<>();


    public RequestBase(@NotNull String host) {
        this.host = host;
        // defaults
        this.httpMethod = HttpMethod.GET;
        addHeader("content-type", "application/json");
    }

    public RequestBase(@NotNull String host, @NotNull String route) {
        this(host);
        this.route = route;
    }

    public RequestBase setHttpMethod(@NotNull HttpMethod httpMethod) {
        this.httpMethod = httpMethod;

        return this;
    }

    public @NotNull HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public RequestBase setBody(String body) {
        this.body = body;

        return this;
    }

    public String getBody() {
        return body;
    }

    public RequestBase setRoute(@NotNull String route) {
        this.route = route;

        return this;
    }

    public @NotNull String getRoute() {
        return route;
    }

    public RequestBase addHeader(@NotNull String name, @NotNull String value) {
        this.headers.put(name, value);

        return this;
    }

    public RequestBase addHeaders(@NotNull HashMap<String, String> headers) {
        this.headers.putAll(headers);

        return this;
    }

    public RequestBase setHeader(@NotNull String name, @NotNull String value) {
        this.headers = new HashMap<>(){{put(name, value);}};

        return this;
    }

    public RequestBase setHeaders(@NotNull Map<String, String> headers) {
        this.headers = new HashMap<>(headers);

        return this;
    }

    protected @NotNull HeaderBag getHeaders() {
        HeaderBag.Builder headerBag = new HeaderBag.Builder();

        for (Map.Entry<String, String> header: headers.entrySet()) {
            headerBag.set(header.getKey(), header.getValue());
        }

        return headerBag.build();
    }

    public RequestBase addParam(@NotNull String name, @NotNull String value) {
        this.params.put(name, value);

        return this;
    }

    public RequestBase addParams(@NotNull Map<String, String> params) {
        this.params.putAll(params);

        return this;
    }

    public RequestBase setParam(@NotNull String name, @NotNull String value) {
        this.params = new HashMap<>(){{put(name, value);}};

        return this;
    }

    public RequestBase setParams(@NotNull Map<String, String> params) {
        this.params = new HashMap<>(params);

        return this;
    }

    public @NotNull HashMap<String, String> getParams() {
        return new HashMap<>(params);
    }

    public @NotNull String getEndpoint() {
        return General.createEndpoint(this.host, this.route);
    }

    protected @NotNull Request.Builder createBuilder() {
        builder.setUrl(General.createEndpoint(this.host, this.route));
        builder.setMethod(httpMethod);
        builder.setHeaders(getHeaders());
        builder.setParams(getParams());

        // must be done like that
        if (body == null) {
            builder.setBody((byte[])null);
        } else {
            builder.setBody(body);
        }

        return builder;
    }

    public @NotNull Response execute() throws UnsuccessfulResponseException, MalformedURLException, CommunicationException {
        return createBuilder().execute();
    }
}
