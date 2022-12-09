package com.github.libgraviton.workerbase.gdk.api.gateway;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.header.Header;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.multipart.FilePart;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulRequestException;
import io.activej.inject.annotation.Inject;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@GravitonWorkerDiScan
public class OkHttpGateway implements GravitonGateway {

    @Inject
    OkHttpClient okHttp;

    @Inject
    public OkHttpGateway() {

    }

    public OkHttpClient getOkHttp() {
        return okHttp;
    }

    public Response execute(Request request) throws CommunicationException {
        okhttp3.Request okHttpRequest = generateRequest(request);

        try (okhttp3.Response okHttpResponse = okHttp.newCall(okHttpRequest).execute()) {
            byte[] body = okHttpResponse.body().bytes();
            return generateResponse(request, okHttpResponse, body);
        } catch (IOException e) {
            throw new UnsuccessfulRequestException(
                    String.format("'%s' to '%s' failed.", request.getMethod(), request.getUrl()),
                    e
            );
        }
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
        // string based parts
        for (Part part : request.getParts()) {
            MultipartBody.Part bodyPart;
            RequestBody requestBody = RequestBody.create(part.getBody());
            if (part.getFormName() != null) {
                bodyPart = MultipartBody.Part.createFormData(part.getFormName(), null, requestBody);
            } else {
                bodyPart = MultipartBody.Part.create(null, requestBody);
            }

            builder.addPart(bodyPart);
        }
        // file parts
        for (FilePart part : request.getFileParts()) {
            MultipartBody.Part bodyPart;
            RequestBody requestBody = RequestBody.create(part.getBody(), MediaType.parse(part.getContentType()));
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
                RequestBody.create(request.getBodyBytes(), MediaType.parse(request.getHeaders().get("Content-Type") + "; charset=utf-8"));
    }

    private Response generateResponse(Request request, okhttp3.Response okHttpResponse, byte[] body) {
        Response.Builder responseBuilder = new Response.Builder(request);
        return responseBuilder
                .code(okHttpResponse.code())
                .headers(createResponseHeaders(okHttpResponse.headers()))
                .message(okHttpResponse.message())
                .successful(okHttpResponse.isSuccessful())
                .body(body)
                .build();
    }

    private Headers createRequestHeaders(HeaderBag headerBag) {
        Headers.Builder builder = new Headers.Builder();
        for (Map.Entry<String, Header> header : headerBag.all().entrySet()) {
            for (String value : header.getValue()) {
                builder.add(header.getKey(), value);
            }
        }
        return builder.build();
    }

    private HeaderBag.Builder createResponseHeaders(Headers okhttpHeaders) {
        HeaderBag.Builder builder = new HeaderBag.Builder();
        for (Map.Entry<String, List<String>> header : okhttpHeaders.toMultimap().entrySet()) {
            builder.set(header.getKey(), header.getValue());
        }
        return builder;
    }

}
