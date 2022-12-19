package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.gateway.GravitonGateway;
import com.github.libgraviton.workerbase.gdk.api.multipart.Part;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulResponseException;
import com.github.libgraviton.workerbase.gdk.requestexecutor.auth.Authenticator;
import com.github.libgraviton.workerbase.gdk.requestexecutor.exception.AuthenticatorException;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import com.google.common.base.Stopwatch;
import io.activej.inject.annotation.Inject;
import io.activej.inject.annotation.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base class used for Graviton API calls.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
@GravitonWorkerDiScan
public class RequestExecutor {

    @Provides
    public static RequestExecutor getInstance(ObjectMapper objectMapper, GravitonGateway gateway) {
        return new RequestExecutor(objectMapper, gateway);
    }

    private static final Logger LOG = LoggerFactory.getLogger(RequestExecutor.class);

    /**
     * The object mapper used to serialize / deserialize to / from JSON
     */
    private final ObjectMapper objectMapper;

    /**
     * The http client for making http calls.
     */
    private final GravitonGateway gateway;

    protected Authenticator authenticator;

    @Inject
    public RequestExecutor(ObjectMapper objectMapper, GravitonGateway gateway) {
        this.gateway = gateway;
        this.objectMapper = objectMapper;
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

        final Stopwatch reqStopwatch = Stopwatch.createStarted();

        final Response response = gateway.execute(request);
        response.setObjectMapper(getObjectMapper());

        // special treatment for event status
        String addedText = "";
        if (request.getUrl().getPath().startsWith("/event/status/")) {
            EventStatusStatus.Status containingStatus = null;
            for (EventStatusStatus.Status status : EventStatusStatus.Status.values()) {
                if (request.getBody() != null && request.getBody().contains("\""+status.value()+"\"")) {
                    containingStatus = status;
                }
            }

            if (containingStatus != null) {
                addedText = " [EventStatus set to \"" + containingStatus + "\"]";
            }
        }

        LOG.info(
                "REQ '{}' to '{}'. RES code '{}' in '{}' ms.{}",
                request.getMethod(),
                request.getUrl(),
                response.getCode(),
                reqStopwatch.elapsed().toMillis(),
                addedText
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

    public GravitonGateway getGateway() {
        return gateway;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
