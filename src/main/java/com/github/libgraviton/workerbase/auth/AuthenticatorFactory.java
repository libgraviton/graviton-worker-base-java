package com.github.libgraviton.workerbase.auth;

import com.github.libgraviton.workerbase.auth.exception.UnknownAuthenticator;
import java.util.Properties;
import javax.naming.AuthenticationException;

/**
 * Resolver for auth strategy
 */
public class AuthenticatorFactory {

    private String strategy;
    private String gatewayUrl;
    private String gatewayUser;
    private String gatewayPassword;

    /**
     * constructor
     *
     * @param properties properties
     */
    public AuthenticatorFactory(Properties properties) {
        strategy = properties.getProperty("worker.auth.strategy", "");
        gatewayUrl = properties.getProperty("fil.endpoint.host");
        gatewayUser = properties.getProperty("worker.auth.gravitonGateway.authUsername");
        gatewayPassword = properties.getProperty("worker.auth.gravitonGateway.authPassword");
    }

    /**
     * Gets the strategy depending on the subscriptions
     *
     * @param coreUserId The core user id
     * @return Authenticator the choosen auth strategy
     */
    public Authenticator create(String coreUserId) throws UnknownAuthenticator {
        Authenticator authenticator;

        if (strategy.equals("GravitonGateway")) {
            authenticator = new GravitonGatewayAuth(gatewayUrl, gatewayUser, gatewayPassword);
        } else if (strategy.equals("Dummy")) {
            authenticator = new DummyAuth();
        } else {
            throw new UnknownAuthenticator("Unknown authenticator '" + strategy + "'.");
        }

        authenticator.setCoreUserId(coreUserId);

        return authenticator;
    }
}
