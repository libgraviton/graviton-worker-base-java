package com.github.libgraviton.workerbase.gdk.api.gateway;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.header.Header;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.multipart.FilePart;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.github.mizosoft.methanol.MutableRequest;
import io.activej.inject.annotation.Inject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@GravitonWorkerDiScan
public class MethanolGateway implements GravitonGateway {

    @Inject
    Methanol httpClient;

    @Inject
    public MethanolGateway() {

    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public Response execute(Request request) throws CommunicationException {
        try {
            HttpRequest req = generateRequest(request);

            HttpResponse<byte[]> response = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
            return generateResponse(request, response);
        } catch (FileNotFoundException | URISyntaxException e) {
          throw new CommunicationException("Unable to create request", e);
        } catch (IOException | InterruptedException e) {
          throw new CommunicationException("Error sending request", e);
        }
    }

    private HttpRequest generateRequest(Request request) throws FileNotFoundException, URISyntaxException {
        HttpRequest.BodyPublisher body;

        if (request.isMultipartRequest()) {
            body = generateMultipartRequestBody(request);
        } else {
            if (null == request.getBodyBytes()) {
                body = HttpRequest.BodyPublishers.noBody();
            } else {
                body = HttpRequest.BodyPublishers.ofByteArray(request.getBodyBytes());
            }
        }

        HttpRequest.Builder builder = MutableRequest.newBuilder()
          .uri(request.getUrl().toURI())
          .method(request.getMethod().asString(), body);

        // set headers
        for (Map.Entry<String, Header> header : request.getHeaders().all().entrySet()) {
            for (String value : header.getValue()) {
                builder.header(header.getKey(), value);
            }
        }

        return builder.build();
    }

    private MultipartBodyPublisher generateMultipartRequestBody(Request request) throws FileNotFoundException {
        MultipartBodyPublisher.Builder multipartBuilder = MultipartBodyPublisher.newBuilder();

        int partNumber = 1;
        for (Part part : request.getParts()) {
            if (part.getFormName() != null) {
                multipartBuilder.formPart(part.getFormName(), HttpRequest.BodyPublishers.ofByteArray(part.getBody()));
            } else {
                String partName = String.format("part-%s", partNumber);
                multipartBuilder.formPart(partName, HttpRequest.BodyPublishers.ofByteArray(part.getBody()));
            }
            partNumber++;
        }

        for (FilePart part : request.getFileParts()) {
            if (part.getFormName() != null) {
                multipartBuilder.filePart(part.getFormName(), part.getBody().toPath());
            } else {
                String partName = String.format("part-%s", partNumber);
                multipartBuilder.filePart(partName, part.getBody().toPath(), com.github.mizosoft.methanol.MediaType.parse(part.getContentType()));
            }
            partNumber++;
        }

        return multipartBuilder.build();
    }

    private Response generateResponse(Request req, HttpResponse<byte[]> resp) {
        Response.Builder responseBuilder = new Response.Builder(req);

        return responseBuilder
                .code(resp.statusCode())
                .headers(createResponseHeaders(resp.headers()))
                .successful(resp.statusCode() < 300)
                .body(resp.body())
                .build();
    }

    private HeaderBag.Builder createResponseHeaders(HttpHeaders headers) {
        HeaderBag.Builder builder = new HeaderBag.Builder();
        headers.map().forEach(builder::set);
        return builder;
    }

}
