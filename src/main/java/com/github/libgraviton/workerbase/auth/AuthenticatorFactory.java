package com.github.libgraviton.workerbase.auth;

import com.github.libgraviton.workerbase.auth.exception.UnknownAuthenticator;

/**
 * Resolver for auth strategy
 */
public class AuthenticatorFactory {

    /**
     * Gets the strategy depending on the subscriptions
     *
     * @param strategy strategy
     * @param gatewayUrl gateway url
     * @param gatewayUser gateway technical user name
     * @param gatewayPassword gateway technical user password
     *
     * @return Authenticator the choosen auth strategy
     */
    public static Authenticator create(
        String strategy,
        String gatewayUrl,
        String gatewayUser,
        String gatewayPassword
    ) throws UnknownAuthenticator {
        Authenticator authenticator;

        if (strategy.equals("GravitonGateway")) {
            authenticator = new GravitonGatewayAuth(gatewayUrl, gatewayUser, gatewayPassword);
        } else if (strategy.equals("Dummy")) {
            authenticator = new DummyAuth();
        } else {
            throw new UnknownAuthenticator("Unknown authenticator '" + strategy + "'.");
        }

        //authenticator.setCoreUserId(coreUserId);

        return authenticator;
    }
}
