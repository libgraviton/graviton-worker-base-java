package com.github.libgraviton.workerbase.auth.request;

import com.github.libgraviton.gdk.RequestExecutor;
import com.github.libgraviton.gdk.api.Request;
import com.github.libgraviton.gdk.api.Response;
import com.github.libgraviton.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.auth.Authenticator;
import com.github.libgraviton.workerbase.auth.exception.CannotProcessAuth;
import com.github.libgraviton.workerbase.helper.WorkerUtil;

/**
 * represents an impersonated graviton gateway request
 */
public class ImpersonatedGatewayRequest extends Request {

    public static class Builder extends Request.Builder {

        private Authenticator authenticator;
        private String coreUserId;
        private boolean trustAllCertificates;

        /**
         * constructor
         *
         * @param executor request executor
         * @param authenticator authenticator
         */
        public Builder(RequestExecutor executor, Authenticator authenticator) {
            super(executor);
            this.authenticator = authenticator;
        }

        public Builder trustAllCertificates(boolean trustAllCertificates) {
            this.trustAllCertificates = trustAllCertificates;
            return this;
        }

        /**
         * set core user id to authenticate against.
         *
         * @param coreUserId core user id
         * @return this
         */
        public Builder authenticateAs(String coreUserId) {
            this.coreUserId = coreUserId;

            return this;
        }

        /**
         * executes a request.
         *
         * @return response
         * @throws CommunicationException communication exception
         */
        public Response execute() throws CommunicationException {
            Response response;

            try {
                authenticator.setCoreUserId(coreUserId);
                authenticator.beforeRequest(this);

                if (trustAllCertificates) {
                    try {
                        executor.setGateway(WorkerUtil.getAllTrustingGatewayInstance());
                    } catch (Exception e) {
                        throw new CommunicationException("Unable to get all trusting OkHttpGateway instance");
                    }
                }

                response = super.execute();
                authenticator.onResponse(response);
            } catch (CannotProcessAuth e) {
                throw new CommunicationException("Authentication failed.", e);
            } catch (CommunicationException e) {
                authenticator.onRequestFailure();
                throw e;
            }

            return response;
        }

        /**
         * return the body.
         *
         * @return body
         */
        public String getBody() {
            return body == null ? null : new String(body);
        }

        /**
         * sets the body.
         *
         * @param body body
         *
         * @return body as Request.Builder
         */
        public Request.Builder setBody(String body) {
            return body == null ? null : super.setBody(body);
        }
    }
}
