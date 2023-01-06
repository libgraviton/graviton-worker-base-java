package com.github.libgraviton.workerbase.gdk;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.multipart.FilePart;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workerbase.util.DownloadClient;

import java.io.IOException;

/**
 * Extra Graviton API functionality for /file endpoint calls.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @version $Id: $Id
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 */
public record GravitonFileEndpoint(GravitonApi gravitonApi) {

    /**
     * gets a temp file..
     *
     * @return
     * @throws IOException
     */
    public java.io.File getTempFile() throws IOException {
        return java.io.File.createTempFile("grv", null);
    }

    /**
     * get a specific File by id (the metadata)
     *
     * @return
     */
    public File getFileMetadata(String urlOrId) throws GravitonCommunicationException {
        try {
            return gravitonApi.get(gravitonApi.getIdFromUrlOrId(urlOrId), File.class).execute().getBodyItem(File.class);
        } catch (CommunicationException ce) {
            throw new GravitonCommunicationException("Error fetching file metadata", ce);
        }
    }

    public File getFileFromQueueEvent(QueueEvent queueEvent) throws GravitonCommunicationException {
        return getFileMetadata(queueEvent.getDocument().get$ref());
    }

    /**
     * Writes the Graviton file *contents* to disk..
     *
     * @param urlOrId
     * @param destinationPath
     */
    public void writeFileContentToDisk(String urlOrId, String destinationPath) {
        String fileId = gravitonApi.getIdFromUrlOrId(urlOrId);
        String fileUrl = getFileDownloadUrl(fileId);

        DownloadClient.downloadFile(fileUrl, destinationPath, gravitonApi.getRequestHeaders());
    }

    @Deprecated(since = "Use writeFileContentToDisk(), File and streams to deal with files, not byte arrays!")
    public byte[] getFileContentAsBytes(String urlOrId) throws IOException {
        String fileId = gravitonApi.getIdFromUrlOrId(urlOrId);
        String fileUrl = getFileDownloadUrl(fileId);
        return DownloadClient.downloadFileBytes(fileUrl);
    }

    public void writeFileContentToDisk(String urlOrId, java.io.File destinationPath) throws Exception {
        writeFileContentToDisk(urlOrId, destinationPath.getAbsolutePath());
    }

    private String getFileDownloadUrl(String id) {
        return gravitonApi.getEndpointManager().getEndpoint(File.class.getName()).getUrl().concat(id);
    }

    public Request.Builder post(byte[] data, GravitonBase resource) throws SerializationException {
        Part dataPart = new Part(data, "upload");
        return getBaseFileMultiPartRequest(resource, HttpMethod.POST).addPart(dataPart);
    }

    public Request.Builder put(byte[] data, GravitonBase resource) throws SerializationException {
        Part dataPart = new Part(data, "upload");
        return getBaseFileMultiPartRequest(resource, HttpMethod.PUT).addPart(dataPart);
    }

    public Request.Builder post(java.io.File file, GravitonBase resource) throws SerializationException {
        FilePart dataPart = new FilePart(file, "upload");
        return getBaseFileMultiPartRequest(resource, HttpMethod.POST).addFilePart(dataPart);
    }

    public Request.Builder put(java.io.File file, GravitonBase resource) throws SerializationException {
        FilePart dataPart = new FilePart(file, "upload");
        return getBaseFileMultiPartRequest(resource, HttpMethod.PUT).addFilePart(dataPart);
    }

    private Request.Builder getBaseFileMultiPartRequest(GravitonBase resource, HttpMethod method) throws SerializationException {
        Part metadataPart = new Part(gravitonApi.serializeResource(resource), "metadata");

        HeaderBag.Builder headers = gravitonApi.getDefaultHeaders()
                .unset("Accept")
                .unset("Content-Type");

        Request.Builder builder = gravitonApi.request()
                .setHeaders(headers.build())
                .addPart(metadataPart)
                .setMethod(method);

        // post or put?
        if (method.equals(HttpMethod.PUT)) {
            // item url
            builder.setUrl(gravitonApi.getEndpointManager().getEndpoint(resource.getClass().getName()).getItemUrl())
                    .addParam("id", gravitonApi.extractId(resource));
        } else {
            builder.setUrl(gravitonApi.getEndpointManager().getEndpoint(resource.getClass().getName()).getUrl());
        }

        return builder;
    }

    public Request.Builder patch(GravitonBase resource) throws SerializationException {
        return gravitonApi.patch(resource);
    }

    public Request.Builder delete(GravitonBase resource) {
        return gravitonApi.delete(resource);
    }

}
