package com.github.libgraviton.gdk.api.gateway;

import com.github.libgraviton.gdk.api.Request;
import com.github.libgraviton.gdk.api.Response;
import com.github.libgraviton.gdk.api.header.Header;
import com.github.libgraviton.gdk.api.header.HeaderBag;
import com.github.libgraviton.gdk.api.multipart.Part;
import com.github.libgraviton.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.exception.UnsuccessfulRequestException;
import com.github.libgraviton.gdk.util.okhttp.interceptor.RetryInterceptor;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpGateway implements GravitonGateway {

    private OkHttpClient okHttp;

    public OkHttpGateway() {
        this(
            new OkHttpClient.Builder()
                    .addInterceptor(new RetryInterceptor())
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()
        );
    }

    public OkHttpGateway(OkHttpClient okHttp) {
        this.okHttp = okHttp;
    }

    public Response execute(Request request) throws CommunicationException {
        okhttp3.Request okHttpRequest = generateRequest(request);

        okhttp3.Response okHttpResponse;
        byte[] body;
        try {
            okHttpResponse = okHttp.newCall(okHttpRequest).execute();
            body = okHttpResponse.body().bytes();
        } catch (IOException e) {
            throw new UnsuccessfulRequestException(
                    String.format("'%s' to '%s' failed.", request.getMethod(), request.getUrl()),
                    e
            );
        }

        return generateResponse(request, okHttpResponse, body);
    }

    private okhttp3.Request generateRequest(Request request) {
        RequestBody okHttpBody;
        if (request.isMultipartRequest()) {
            okHttpBody = generateMultipartRequestBody(request);
        } else {
            okHttpBody = generateDefaultRequestBody(request);
        }

        okhttp3.Request.Builder requestBuilder = new okhttp3.Request.Builder();
        return requestBuilder
                .method(request.getMethod().asString(), okHttpBody)
                .url(request.getUrl())
                .headers(createRequestHeaders(request.getHeaders()))
                .build();
    }

    private RequestBody generateMultipartRequestBody(Request request) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (Part part : request.getParts()) {
            MultipartBody.Part bodyPart;
            RequestBody requestBody = RequestBody.create(null, part.getBody());
            if (part.getFormName() != null) {
                bodyPart = MultipartBody.Part.createFormData(part.getFormName(), null, requestBody);
            } else {
                bodyPart = MultipartBody.Part.create(null, requestBody);
            }

            builder.addPart(bodyPart);
        }

        return builder.build();
    }

    private RequestBody generateDefaultRequestBody(Request request) {
        return null == request.getBodyBytes() ? null :
                RequestBody.create(MediaType.parse(request.getHeaders().get("Content-Type") + "; charset=utf-8"), request.getBodyBytes());
    }

    private com.github.libgraviton.gdk.api.Response generateResponse(Request request, okhttp3.Response okHttpResponse, byte[] body) {
        com.github.libgraviton.gdk.api.Response.Builder responseBuilder = new com.github.libgraviton.gdk.api.Response.Builder(request);
        return responseBuilder
                .code(okHttpResponse.code())
                .headers(createResponseHeaders(okHttpResponse.headers()))
                .message(okHttpResponse.message())
                .successful(okHttpResponse.isSuccessful())
                .body(body)
                .build();
    }

    protected Headers createRequestHeaders(HeaderBag headerBag) {
        Headers.Builder builder = new Headers.Builder();
        for (Map.Entry<String, Header> header : headerBag.all().entrySet()) {
            for (String value : header.getValue()) {
                builder.add(header.getKey(), value);
            }
        }
        return builder.build();
    }

    protected HeaderBag.Builder createResponseHeaders(Headers okhttpHeaders) {
        HeaderBag.Builder builder = new HeaderBag.Builder();
        for (Map.Entry<String, List<String>> header : okhttpHeaders.toMultimap().entrySet()) {
            builder.set(header.getKey(), header.getValue());
        }
        return builder;
    }

}
