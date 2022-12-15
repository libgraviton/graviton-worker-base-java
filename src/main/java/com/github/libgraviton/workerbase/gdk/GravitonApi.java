package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.api.NoopRequest;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointManager;
import com.github.libgraviton.workerbase.gdk.api.endpoint.GeneratedEndpointManager;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.query.rql.Rql;
import com.github.libgraviton.workerbase.gdk.auth.HeaderAuth;
import com.github.libgraviton.workerbase.gdk.auth.NoAuth;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import com.github.libgraviton.workerbase.gdk.serialization.JsonPatcher;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.RqlObjectMapper;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import io.activej.inject.annotation.Inject;
import io.activej.inject.annotation.Provides;
import io.activej.inject.annotation.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the base class used for Graviton API calls.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
@GravitonWorkerDiScan
public class GravitonApi {

    @Provides
    @Transient
    public static GravitonApi getInstance(GeneratedEndpointManager endpointManager, ObjectMapper objectMapper, RqlObjectMapper rqlObjectMapper) {
        return new GravitonApi(endpointManager, objectMapper, rqlObjectMapper);
    }

    private static final Logger LOG = LoggerFactory.getLogger(GravitonApi.class);

    /**
     * Defines the base setUrl of the Graviton server
     */
    private final String baseUrl;
    private final String authHeaderName;
    private final String authHeaderValue;
    private final Map<String, String> transientHeaders = new HashMap<>();

    /**
     * The object mapper used to serialize / deserialize to / from JSON
     */
    private final ObjectMapper objectMapper;
    private final RqlObjectMapper rqlObjectMapper;

    /**
     * The endpoint manager which is used
     */
    private final EndpointManager endpointManager;

    private HeaderAuth auth;

    @Inject
    public GravitonApi(GeneratedEndpointManager endpointManager, ObjectMapper objectMapper, RqlObjectMapper rqlObjectMapper) {
        this.baseUrl = WorkerProperties.getProperty(WorkerProperties.GRAVITON_BASE_URL);
        this.authHeaderValue = WorkerProperties.getProperty(WorkerProperties.AUTH_PREFIX_USERNAME)
                .concat(WorkerProperties.getProperty(WorkerProperties.WORKER_ID));
        this.authHeaderName = WorkerProperties.getProperty(WorkerProperties.AUTH_HEADER_NAME);
        this.endpointManager = endpointManager;
        this.objectMapper = objectMapper;
        this.rqlObjectMapper = rqlObjectMapper;
        this.auth = new NoAuth();
    }

    public void setAuth(HeaderAuth auth) {
        this.auth = auth;
    }

    /**
     * if an url is passed, the id is returned. if it seems to be an id, only that id is returned..
     * @param urlOrId
     * @return
     */
    public String getIdFromUrlOrId(String urlOrId) {
        if (!urlOrId.contains("/")) {
            return urlOrId;
        }

        return urlOrId.substring(urlOrId.lastIndexOf("/") + 1);
    }

    /**
     * Returns the endpoint manager
     *
     * @return The endpoint manager
     */
    public EndpointManager getEndpointManager() {
        return endpointManager;
    }

    /**
     * Returns the base url
     *
     * @return The base url
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Creates a new executable request builder to build and perform a new request.
     *
     * @return A new executable request builder
     */
    public Request.Builder request() {
        return new Request.Builder()
                .setHeaders(getDefaultHeaders().build());
    }

    public Request.Builder head(String url) {
        return request().setUrl(url).head();
    }

    public Request.Builder head(GravitonBase resource) {
        return head(extractId(resource), resource.getClass());
    }

    public Request.Builder head(String id, Class clazz) {
        return head(endpointManager.getEndpoint(clazz.getName()).getUrl())
                .addParam("id", id);
    }

    public Request.Builder options(String url) {
        return request().setUrl(url).options();
    }

    public Request.Builder options(GravitonBase resource) {
        return options(extractId(resource), resource.getClass());
    }

    public Request.Builder options(String id, Class clazz) {
        return options(endpointManager.getEndpoint(clazz.getName()).getUrl())
                .addParam("id", id);
    }

    public Request.Builder get(String url) {
        return request().setUrl(url).get();
    }

    public Request.Builder get(GravitonBase resource) {
        return get(extractId(resource), resource.getClass());
    }

    public Request.Builder get(String id, Class clazz) {
        return get(endpointManager.getEndpoint(clazz.getName()).getItemUrl())
                .addParam("id", id);
    }

    /**
     * GET request with a URL query. The response will result in a list of items,
     * even if there is only 1 matching item to the query.
     *
     * @param resource payload
     * @return request builder
     */
    public Request.Builder query(GravitonBase resource) {
        return get(endpointManager.getEndpoint(resource.getClass().getName()).getUrl())
                .setQuery(new Rql.Builder().setResource(resource, rqlObjectMapper).build());
    }

    public Request.Builder delete(String url) {
        return request().setUrl(url).delete();
    }

    public Request.Builder delete(GravitonBase resource) {
        return delete(extractId(resource), resource.getClass());
    }

    public Request.Builder delete(String id, Class clazz) {
        return delete(endpointManager.getEndpoint(clazz.getName()).getItemUrl())
                .addParam("id", id);
    }

    public Request.Builder put(GravitonBase resource) throws SerializationException {
        return request()
                .setUrl(endpointManager.getEndpoint(resource.getClass().getName()).getItemUrl())
                .addParam("id", extractId(resource))
                .put(serializeResource(resource));
    }

    public Request.Builder patch(GravitonBase resource) throws SerializationException {
        JsonNode jsonNode = getObjectMapper().convertValue(resource, JsonNode.class);
        String data = serializeResource(JsonPatcher.getPatch(resource, jsonNode));

        Request.Builder builder;
        if(data == null || data.isEmpty() || "[]".equals(data)) {
            builder = new NoopRequest.Builder("PATCH body is empty. Nothing changed.");
        } else {
            builder = request();
        }

        return builder
                .setUrl(endpointManager.getEndpoint(resource.getClass().getName()).getItemUrl())
                .addParam("id", extractId(resource))
                .patch(data);
    }

    public Request.Builder post(GravitonBase resource) throws SerializationException {
        return request()
                .setUrl(endpointManager.getEndpoint(resource.getClass().getName()).getUrl())
                .post(serializeResource(resource));
    }

    /**
     * Extracts the id of a given Graviton resource.
     *
     * @param data The Graviton resource.
     *
     * @return The extracted id.
     */
    protected String extractId(GravitonBase data) {
        String id = data.getId();
        return id != null ? id : "";
    }

    protected String serializeResource(Object data) throws SerializationException {
        if (data == null) {
            return "";
        }

        try {
            return getObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException(
                    String.format("Cannot serialize '%s' to json.", data.getClass().getName()),
                    e
            );
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    protected HeaderBag.Builder getDefaultHeaders() {
        HeaderBag.Builder builder = new HeaderBag.Builder()
                .set("Content-Type", "application/json")
                .set("Accept", "application/json")
                .set(authHeaderName, authHeaderValue);

        // transient headers?
        if (!transientHeaders.isEmpty()) {
            for (Map.Entry<String, String> entry : transientHeaders.entrySet()) {
                builder.set(entry.getKey(), entry.getValue());
            }

            LOG.info("Including transient headers from QueueEvent in request: {}", transientHeaders);
        }

        auth.addHeader(builder);

        return builder;
    }

    /**
     * transient headers as those that we receive in {@link com.github.libgraviton.workerbase.model.QueueEvent}
     * and forward subsequently again on following requests during the request handling in the worker..
     *
     * @param transientHeaders
     */
    public void setTransientHeaders(Map<String, String> transientHeaders) {
        this.transientHeaders.clear();
        this.transientHeaders.putAll(transientHeaders);
    }
}
