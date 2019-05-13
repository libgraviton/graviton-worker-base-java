package com.github.libgraviton.gdk.api;

import com.github.libgraviton.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.exception.UnsuccessfulRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;

public class NoopRequest extends Request {

    private static final Logger LOG = LoggerFactory.getLogger(NoopRequest.class);

    protected String reason;

    protected NoopRequest(Builder builder) throws MalformedURLException {
        super(builder);
        this.reason = builder.reason;
    }

    public String getReason() {
        return reason;
    }

    public static class Builder extends Request.Builder {

        private String reason;

        public Builder(String reason) {
            this.reason = reason;
        }

        public NoopRequest build() throws MalformedURLException {
            return new NoopRequest(this);
        }

        public NoopResponse execute() throws CommunicationException {
            NoopRequest request;
            try {
                request = build();
            } catch (MalformedURLException e) {
                throw new UnsuccessfulRequestException(String.format("'%s' to '%s' failed due to malformed url.", method, url),
                        e);
            }
            LOG.info(request.getMethod() + " request to '" + request.getUrl() + "' not executed due to '" + request.getReason() + "'.");
            return new NoopResponse(request);
        }
    }
}
