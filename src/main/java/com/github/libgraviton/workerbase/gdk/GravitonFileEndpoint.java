package com.github.libgraviton.workerbase.gdk;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import com.github.libgraviton.workerbase.util.DownloadClient;

/**
 * Extra Graviton API functionality for /file endpoint calls.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class GravitonFileEndpoint {

    private final GravitonApi gravitonApi;

    public GravitonFileEndpoint(GravitonApi gravitonApi) {
        this.gravitonApi = gravitonApi;
    }

    /**
     * get a specific File by id (the metadata)

     * @return
     */
    public File getFileMetadata(String urlOrId) throws GravitonCommunicationException {
        try {
            return gravitonApi.get(gravitonApi.getIdFromUrlOrId(urlOrId), File.class).execute().getBodyItem(File.class);
        } catch (CommunicationException ce) {
            throw new GravitonCommunicationException("Error fetching file metadata", ce);
        }
    }

    /**
     * Writes the Graviton file *contents* to disk..
     *
     * @param urlOrId
     * @param destinationPath
     */
    public void writeFileContentToDisk(String urlOrId, String destinationPath) throws Exception {
        String fileId = gravitonApi.getIdFromUrlOrId(urlOrId);
        String fileUrl = getFileDownloadUrl(fileId);

        DownloadClient.downloadFile(fileUrl, destinationPath, true);
    }

    private String getFileDownloadUrl(String id) {
        return gravitonApi.getEndpointManager().getEndpoint(File.class.getName()).getUrl() + id;
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
