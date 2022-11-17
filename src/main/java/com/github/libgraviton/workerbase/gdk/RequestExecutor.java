package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.gateway.GravitonGateway;
import com.github.libgraviton.workerbase.gdk.api.gateway.OkHttpGateway;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulResponseException;
import com.github.libgraviton.workerbase.gdk.requestexecutor.auth.Authenticator;
import com.github.libgraviton.workerbase.gdk.requestexecutor.exception.AuthenticatorException;
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

    protected Authenticator authenticator;

    public RequestExecutor() {
        this(new ObjectMapper());
    }

    public RequestExecutor(ObjectMapper objectMapper) {
        this.gateway = new OkHttpGateway();
        this.objectMapper = objectMapper;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void doTrustEverybody() {
        try {
            gateway.doTrustEverybody();
        } catch (Exception e) {
            LOG.error("Could not trust everybody", e);
        }

        LOG.info("We are going to trust all certificates...");
    }

    /**
     * forces HTTP1 on the request execution
     */
    public void forceHttp1() {
        gateway.forceHttp1();

        LOG.info("Forcing HTTP/1.*");
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
        if (authenticator != null) {
            try {
                request = authenticator.onRequest(request);
            } catch (AuthenticatorException e) {
                throw new CommunicationException("Authenticator exception", e);
            }
        }

        if(LOG.isDebugEnabled()) {
            logBody(request);
        }

        Response response = gateway.execute(request);
        response.setObjectMapper(getObjectMapper());

        LOG.info(
                "Request '{}' to '{}' ended with code {}.",
                request.getMethod(),
                request.getUrl(),
                response.getCode()
        );

        if (!response.isSuccessful()) {
            throw new UnsuccessfulResponseException(response);
        }
        return response;
    }

    public void close() {
        if (authenticator != null) {
            try {
                authenticator.onClose();
            } catch (AuthenticatorException e) {
                LOG.error("Error on Authenticator.close()", e);
            }
        }
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
        LOG.debug("with request body '{}'", body);
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
        LOG.debug("with multipart request body [\n{}]", builder);
    }

    public void setGateway(GravitonGateway gateway) {
        this.gateway = gateway;
    }

    public GravitonGateway getGateway() {
        return gateway;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
