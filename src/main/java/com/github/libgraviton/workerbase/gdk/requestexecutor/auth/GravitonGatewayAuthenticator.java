package com.github.libgraviton.workerbase.gdk.requestexecutor.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.requestexecutor.exception.AuthenticatorException;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

public class GravitonGatewayAuthenticator implements Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(GravitonGatewayAuthenticator.class);
    private final String gatewayUrl;
    private final String gatewayUser;
    private final String gatewayPassword;
    private final String coreUserId;
    private String accessToken;
    private final String tokenHeaderName = "x-rest-token";

    public GravitonGatewayAuthenticator(String gatewayUrl, String gatewayUser, String gatewayPassword) {
        this(gatewayUrl, gatewayUser, gatewayPassword, null);
    }

    public GravitonGatewayAuthenticator(String gatewayUrl, String gatewayUser, String gatewayPassword, String coreUserId) {
        this.gatewayUrl = gatewayUrl.replaceAll("/*$", "");
        this.gatewayUser = gatewayUser;
        this.gatewayPassword = gatewayPassword;
        this.coreUserId = coreUserId;
    }

    @Override
    public Request onRequest(Request request) throws AuthenticatorException {
        if (accessToken == null) {
            login();
        }

        request.getHeaders().set(tokenHeaderName, accessToken);

        return request;
    }

    @Override
    public void onClose() throws AuthenticatorException {
        if (accessToken == null) {
            return;
        }

        Request.Builder requestBuilder = new Request.Builder();

        try {
            requestBuilder
                    .setMethod(HttpMethod.GET)
                    .setUrl(gatewayUrl.concat("/security/logout"))
                    .setHeaders(new HeaderBag.Builder()
                            .set(tokenHeaderName, accessToken)
                            .build()
                    )
                    .execute();

        } catch (Exception e) {
            throw new AuthenticatorException("Unable to logout from Graviton Gateway", e);
        }
    }

    private void login() throws AuthenticatorException {
        StringWriter stringWriter = new StringWriter();
        JSONWriter jsonWriter = new JSONWriter(stringWriter);

        try {
            jsonWriter.object().key("username").value(gatewayUser).key("password").value(gatewayPassword).endObject();
            stringWriter.close();
        } catch (Exception e) {
            throw new AuthenticatorException("Unable to generate JSON for Graviton Gateway /auth.", e);
        }

        String authBody = stringWriter.toString();
        Request.Builder requestBuilder = new Request.Builder();
        JsonNode response;

        // get our auth token
        try {
            HeaderBag.Builder headers = new HeaderBag.Builder();
            headers.set("Content-Type", "application/json");

            // impersonate?
            if (coreUserId != null) {
                LOG.info("Executing POST on {}/auth for user '{}' - impersonating '{}'", gatewayUrl, gatewayUser, coreUserId);
                headers.set("X-Impersonate", coreUserId);
            } else {
                LOG.info("Executing POST on {}/auth for user '{}'", gatewayUrl, gatewayUser);
            }

            Response authResponse = requestBuilder
                    .setMethod(HttpMethod.POST)
                    .setUrl(gatewayUrl.concat("/auth"))
                    .setHeaders(headers.build()).setBody(authBody)
                    .execute();

            response = authResponse.getBodyItem(JsonNode.class);
        } catch (CommunicationException e) {
            throw new AuthenticatorException("Unable to locate token in authServiceUrl response.", e);
        }

        accessToken = response.get("token").asText();

        LOG.info("Successfully received token '{}'", accessToken);
    }
}
