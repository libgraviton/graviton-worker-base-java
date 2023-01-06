package com.github.libgraviton.workerbase.gdk.api;

import com.github.libgraviton.workerbase.gdk.api.NoopResponse;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class NoopRequest extends Request {

    private static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.gdk.api.NoopRequest.class);

    protected String reason;

    protected NoopRequest(Builder builder) throws MalformedURLException {
        super(builder);
        this.reason = builder.reason;
    }

    public String getReason() {
        return reason;
    }

    public static class Builder extends Request.Builder {

        private final String reason;

        public Builder() {
            this("Unknown reason");
        }

        public Builder(String reason) {
            super();
            this.reason = reason;
        }

        public com.github.libgraviton.workerbase.gdk.api.NoopRequest build() throws MalformedURLException {
            return new com.github.libgraviton.workerbase.gdk.api.NoopRequest(this);
        }

        public NoopResponse execute() throws CommunicationException {
            com.github.libgraviton.workerbase.gdk.api.NoopRequest request;
            try {
                request = build();
            } catch (MalformedURLException e) {
                throw new UnsuccessfulRequestException(String.format("'%s' to '%s' failed due to malformed url.", method, url),
                        e);
            }

            LOG.info(
                    "{} request to '{}' not executed due to '{}'.",
                    request.getMethod(),
                    request.getUrl(),
                    request.getReason()
            );

            return new NoopResponse(request);
        }
    }
}
