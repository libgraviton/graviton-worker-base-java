package com.github.libgraviton.workerbase.gdk.exception;

import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;

/**
 * Whenever a received response code is not within 200 - 299.
 */
public class UnsuccessfulResponseException extends CommunicationException {

    private final Response response;

    public UnsuccessfulResponseException(Response response) {
        super(generateMessage(response));
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    private static String generateMessage(Response response) {
        return String.format(
                "Failed '%s' to '%s'. Response was '%d' - '%s' with body '%s'.",
                response.getRequest().getMethod(),
                response.getRequest().getUrl(),
                response.getCode(),
                response.getMessage(),
                response.getBody()
        );
    }

}
