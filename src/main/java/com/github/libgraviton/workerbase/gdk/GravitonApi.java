package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.api.NoopRequest;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointManager;
import com.github.libgraviton.workerbase.gdk.api.endpoint.GeneratedEndpointManager;
import com.github.libgraviton.workerbase.gdk.api.endpoint.exception.UnableToLoadEndpointAssociationsException;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.api.query.rql.Rql;
import com.github.libgraviton.workerbase.gdk.auth.HeaderAuth;
import com.github.libgraviton.workerbase.gdk.auth.NoAuth;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.exception.SerializationException;
import com.github.libgraviton.workerbase.gdk.serialization.JsonPatcher;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.GravitonObjectMapper;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.RqlObjectMapper;
import com.github.libgraviton.workerbase.helper.WorkerProperties;

import java.io.IOException;
import java.util.Properties;

/**
 * This is the base class used for Graviton API calls.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class GravitonApi {

    /**
     * Defines the base setUrl of the Graviton server
     */
    private final String baseUrl;

    /**
     * The object mapper used to serialize / deserialize to / from JSON
     */
    private ObjectMapper objectMapper;

    /**
     * The endpoint manager which is used
     */
    private final EndpointManager endpointManager;

    private Properties properties;

    private RequestExecutor executor;

    private HeaderAuth auth;

    public GravitonApi() {
        this(null);
    }

    public GravitonApi(Properties properties) {
        this.properties = properties;
        this.baseUrl = properties.getProperty("graviton.base.url");

        setup();

        try {
            this.endpointManager = initEndpointManager();
        } catch (UnableToLoadEndpointAssociationsException e) {
            throw new IllegalStateException(e);
        }
    }

    public GravitonApi(String baseUrl, EndpointManager endpointManager) {
        setup();
        this.baseUrl = baseUrl;
        this.endpointManager = endpointManager;
    }

    public GravitonApi(String baseUrl, GeneratedEndpointManager endpointManager, HeaderAuth auth) {
        this(baseUrl, endpointManager);
        this.auth = auth;
    }

    /**
     * if an url is passed, the id is returned.. if it seems to be an id, only that id is returned..
     * @param urlOrId
     * @return
     */
    public String getIdFromUrlOrId(String urlOrId) {
        if (!urlOrId.contains("/")) {
            return urlOrId;
        }

        return urlOrId.substring(urlOrId.lastIndexOf("/") + 1);
    }

    protected void setup() {
        if (properties == null) {
            try {
                properties = WorkerProperties.load();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to load properties files.", e);
            }
        }
        this.auth = new NoAuth();
        this.objectMapper = GravitonObjectMapper.getInstance(properties);
        this.executor = new RequestExecutor(objectMapper);
    }

    protected GeneratedEndpointManager initEndpointManager() throws UnableToLoadEndpointAssociationsException {
        return new GeneratedEndpointManager();
    }

    /**
     * Returns the endpoint manager
     *
     * @return The endpoint manager
     */
    public EndpointManager getEndpointManager() {
        return endpointManager;
    }

    public RequestExecutor getRequestExecutor() {
        return executor;
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
        return new Request.Builder(executor)
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
                .setQuery(new Rql.Builder().setResource(resource, new RqlObjectMapper(properties)).build());
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

    // TODO make it configurable
    protected HeaderBag.Builder getDefaultHeaders() {
        HeaderBag.Builder headerBuilder = new HeaderBag.Builder()
                .set("Content-Type", "application/json")
                .set("Accept", "application/json");
        auth.addHeader(headerBuilder);

        return headerBuilder;
    }
}
