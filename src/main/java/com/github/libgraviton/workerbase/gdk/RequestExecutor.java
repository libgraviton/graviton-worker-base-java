package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.gateway.GravitonGateway;
import com.github.libgraviton.workerbase.gdk.api.gateway.OkHttpGateway;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class used for Graviton API calls.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class RequestExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(RequestExecutor.class);

    /**
     * The object mapper used to serialize / deserialize to / from JSON
     */
    protected ObjectMapper objectMapper;

    /**
     * The http client for making http calls.
     */
    protected GravitonGateway gateway;

    public RequestExecutor() {
        this(new ObjectMapper());
    }

    public RequestExecutor(ObjectMapper objectMapper) {
        this.gateway = new OkHttpGateway();
        this.objectMapper = objectMapper;
    }

    /**
     * forces HTTP1 on the request execution
     */
    public void forceHttp1() {
        gateway.forceHttp1();
    }

    /**
     * Executes a given Graviton request.
     *
     * @param request The Graviton request
     *
     * @return The corresponding Graviton response
     *
     * @throws CommunicationException If the request was not successful
     */
    public Response execute(Request request) throws CommunicationException {
        LOG.info(String.format("Starting '%s' to '%s'...", request.getMethod(), request.getUrl()));
        if(LOG.isDebugEnabled()) {
            logBody(request);
        }

        Response response = gateway.execute(request);
        response.setObjectMapper(getObjectMapper());

        if(response.isSuccessful()) {
            LOG.info(String.format(
                    "Successful '%s' to '%s'. Response was '%d' - '%s'.",
                    request.getMethod(),
                    request.getUrl(),
                    response.getCode(),
                    response.getMessage()
            ));
        } else {
            throw new UnsuccessfulResponseException(response);
        }
        return response;
    }

    protected void logBody(Request request) {
        if (request.getBody() != null) {
            logStandardRequest(request);
        }

        if (request.getParts() != null && request.getParts().size() > 0) {
            logMultipartRequest(request);
        }
    }

    private void logStandardRequest(Request request) {
        String body = request.getBody();
        LOG.debug("with request body '" + body + "'");
    }

    private void logMultipartRequest(Request request) {
        StringBuilder builder = new StringBuilder();
        for (Part part: request.getParts()) {
            byte[] body = part.getBody();
            String loggablePart = "Part{" +
                    "formName='" + part.getFormName() + '\'' +
                    ", body='" +
                    new String(body) +
                    '\'' +
                    "}";
            builder.append(loggablePart).append("\n");
        }
        LOG.debug("with multipart request body [\n" + builder.toString() + "]");
    }

    public void setGateway(GravitonGateway gateway) {
        this.gateway = gateway;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
