package com.github.libgraviton.workerbase.gdk;

import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extra Graviton API functionality for /file endpoint calls.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class GravitonFileEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(GravitonFileEndpoint.class);

    private final GravitonApi gravitonApi;

    public GravitonFileEndpoint(GravitonApi gravitonApi) {
        this.gravitonApi = gravitonApi;
    }

    public Request.Builder getFile(String url) {
        LOG.debug("Requesting file {}", url);

        // without the 'Accept' - 'application/json' header, we get the file instead of the metadata
        HeaderBag.Builder headers = gravitonApi.getDefaultHeaders()
                .unset("Accept");

        return gravitonApi.request()
                .setUrl(url)
                .setHeaders(headers.build())
                .get();
    }

    public Request.Builder getFile(GravitonBase resource) {
        return getFile(gravitonApi.extractId(resource), resource.getClass());
    }

    public Request.Builder getFile(String id, Class clazz) {
        return getFile(gravitonApi.getEndpointManager().getEndpoint(clazz.getName()).getItemUrl()).addParam("id", id);
    }

    public Request.Builder getMetadata(String url) {
        LOG.debug("Requesting file metadata {}", url);
        return gravitonApi.get(url);
    }

    public Request.Builder getMetadata(GravitonBase resource) {
        return getMetadata(gravitonApi.extractId(resource), resource.getClass());
    }

    public Request.Builder getMetadata(String id, Class clazz) {
        return getMetadata(gravitonApi.getEndpointManager().getEndpoint(clazz.getName()).getItemUrl()).addParam("id", id);
    }

    public Request.Builder post(byte[] data, GravitonBase resource) throws SerializationException {
        Part dataPart = new Part(data, "upload");
        Part metadataPart = new Part(gravitonApi.serializeResource(resource), "metadata");

        HeaderBag.Builder headers = gravitonApi.getDefaultHeaders()
                .unset("Accept")
                .unset("Content-Type");

        return gravitonApi.request()
                .setUrl(gravitonApi.getEndpointManager().getEndpoint(resource.getClass().getName()).getUrl())
                .setHeaders(headers.build())
                .post(dataPart, metadataPart);
    }

    public Request.Builder put(byte[] data, GravitonBase resource) throws SerializationException {
        Part dataPart = new Part(data, "upload");
        Part metadataPart = new Part(gravitonApi.serializeResource(resource), "metadata");

        HeaderBag.Builder headers = gravitonApi.getDefaultHeaders()
                .unset("Accept")
                .unset("Content-Type");

        return gravitonApi.request()
                .setUrl(gravitonApi.getEndpointManager().getEndpoint(resource.getClass().getName()).getItemUrl())
                .addParam("id", gravitonApi.extractId(resource))
                .setHeaders(headers.build())
                .put(dataPart, metadataPart);
    }

    public Request.Builder patch(GravitonBase resource) throws SerializationException {
        return gravitonApi.patch(resource);
    }

    public Request.Builder delete(GravitonBase resource) {
        return gravitonApi.delete(resource);
    }

}
