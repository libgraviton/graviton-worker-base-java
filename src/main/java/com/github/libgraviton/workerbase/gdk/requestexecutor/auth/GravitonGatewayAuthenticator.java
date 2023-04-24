package com.github.libgraviton.workerbase.gdk.requestexecutor.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.requestexecutor.exception.AuthenticatorException;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GravitonGatewayAuthenticator implements Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(GravitonGatewayAuthenticator.class);
    private final String gatewayUrl;
    private final String gatewayUser;
    private final String gatewayPassword;
    private final String coreUserId;
    private String accessToken;

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

        String tokenHeaderName = "x-rest-token";
        request.getHeaders().set(tokenHeaderName, accessToken);

        return request;
    }

    @Override
    public void close() throws Exception {
        // clear token
        accessToken = null;
        LOG.info("Closed Authenticator; token cleared.");
    }

    private void login() throws AuthenticatorException {
        // do we have a cached token?
        String cachedToken = GatewayAuthTokenCache.getTokenForUser(gatewayUser);
        if (cachedToken != null) {
            LOG.info("Using in a *cached* jwt token for user '{}' (token '{}')", gatewayUser, cachedToken);
            accessToken = cachedToken;
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode loginNode = objectMapper.createObjectNode();

        loginNode.put("username", gatewayUser);
        loginNode.put("password", gatewayPassword);

        String authBody;

        try {
            authBody = objectMapper.writeValueAsString(loginNode);
        } catch (Exception e) {
            throw new AuthenticatorException("Unable to generate JSON for Graviton Gateway /auth.", e);
        }

        Request.Builder requestBuilder = new Request.Builder();
        JsonNode response;

        // get our auth token
        try {
            HeaderBag.Builder headers = new HeaderBag.Builder();
            headers.set("Content-Type", "application/json");

            // token lifetime?
            String tokenLifetime = WorkerProperties.GATEWAY_JWT_LIFETIME.get();
            if (tokenLifetime != null) {
                String lifetimeHeaderName = "x-jwt-lifetime";
                headers.set(lifetimeHeaderName, tokenLifetime);
            }

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

        // cache it!
        GatewayAuthTokenCache.setTokenForUser(gatewayUser, accessToken);

        LOG.info(
                "Successfully received token and cached it for use of max '{}' minutes. (token '{}')",
                GatewayAuthTokenCache.getCacheLifeTime(),
                accessToken
        );
    }

}
