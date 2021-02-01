package com.github.libgraviton.workerbase.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.Request.Builder;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.auth.exception.CannotProcessAuth;
import java.io.StringWriter;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Strategy for authentication using Graviton Gateway.
 *
 * We basically get a token from /auth and include that in all further requests as header. We use the
 * impersonation feature of the gateway to open a token for a different user. for that to work, the
 * configured user in app.properties *must* have the role ROLE_SERVICE assigned.
 */
public class GravitonGatewayAuth extends Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(GravitonGatewayAuth.class);
    private String gatewayUrl;
    private String gatewayUser;
    private String gatewayPassword;
    private String accessToken;
    private String tokenHeaderName = "X-REST-Token";


    /**
     * constructor
     *
     * @param gatewayUrl gateway url
     * @param gatewayUser gateway user
     * @param gatewayPassword gateway password
     */
    public GravitonGatewayAuth(String gatewayUrl, String gatewayUser, String gatewayPassword) {
        this.gatewayUrl = gatewayUrl.replaceAll("/*$", "");
        this.gatewayUser = gatewayUser;
        this.gatewayPassword = gatewayPassword;
    }

    /**
     * @see Authenticator#beforeRequest(Builder)
     */
    @Override
    public Builder beforeRequest(Builder request) throws CannotProcessAuth {
        StringWriter stringWriter = new StringWriter();
        JSONWriter jsonWriter = new JSONWriter(stringWriter);

        try {
            jsonWriter.object().key("username").value(gatewayUser).key("password").value(gatewayPassword).endObject();
            stringWriter.close();
        } catch (Exception e) {
            throw new CannotProcessAuth("Unable to generate JSON for auth.", e);
        }

        String authBody = stringWriter.toString();
        Builder requestBuilder = new Builder();
        JsonNode response;

        LOG.info("Sending to '{}/auth' with 'X-Impersonate': '{}'", gatewayUrl, coreUserId);

        // get our auth token
        try {
            Response authResponse = requestBuilder
                .setMethod(HttpMethod.POST)
                .setUrl(gatewayUrl + "/auth")
                .setHeaders(new HeaderBag.Builder()
                    .set("X-Impersonate", coreUserId)
                    .set("Content-Type", "application/json")
                    .build()
                )
                .setBody(authBody)
                .execute();
            response = authResponse.getBodyItem(JsonNode.class);
            LOG.info("Received HTTP status '{}', body = '{}' from Auth Gateway.", authResponse.getCode(), response.toString());
        } catch (CommunicationException e) {
            throw new CannotProcessAuth("Unable to locate token in authServiceUrl response.", e);
        }

        accessToken = response.get("token").asText();

        LOG.info("Successfully received token '{}' from '{}' impersonating '{}'", accessToken, gatewayUrl, coreUserId);

        request.addHeader(tokenHeaderName, accessToken);

        return request;
    }

    /**
     * @see Authenticator#onResponse(Response)
     */
    public void onResponse(Response response) throws CannotProcessAuth {
        logout();
    }

    /**
     * @see Authenticator#onRequestFailure()
     */
    public void onRequestFailure() {
        try {
            logout();
        } catch (CannotProcessAuth cannotProcessAuth) {
            LOG.warn("Unable to logout during request failure handling.");
        }
    }

    /**
     * logout from gateway to kill our token..
     */
    private void logout() throws CannotProcessAuth {
        Builder requestBuilder = new Builder();

        try {
            requestBuilder
                .setMethod(HttpMethod.GET)
                .setUrl(gatewayUrl + "/security/logout")
                .setHeaders(new HeaderBag.Builder()
                    .set(tokenHeaderName, accessToken)
                    .build()
                )
                .execute();

            LOG.info("Logged out from Auth Gateway");
        } catch (Exception e) {
            throw new CannotProcessAuth("Unable to logout from Auth Gateway", e);
        }
    }
}
