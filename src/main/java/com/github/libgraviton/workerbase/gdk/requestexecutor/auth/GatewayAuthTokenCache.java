package com.github.libgraviton.workerbase.gdk.requestexecutor.auth;

import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.TimeUnit;

class GatewayAuthTokenCache {

    private static Cache<String, String> tokenCache;

    /**
     * how many remaining minutes of lifetime to use a token
     */
    private static final int minimalLifetime = 30;

    public static String getTokenForUser(String username) {
        ensureCacheInstance();
        return tokenCache.getIfPresent(username);
    }

    public static void setTokenForUser(String username, String token) {
        ensureCacheInstance();
        tokenCache.put(username, token);
    }

    private static void ensureCacheInstance() {
        if (tokenCache != null) {
            return;
        }

        tokenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(getCacheLifeTime(), TimeUnit.MINUTES)
                .build();
    }

    public static int getCacheLifeTime() {
        return Integer.parseInt(WorkerProperties.GATEWAY_JWT_LIFETIME.get()) - minimalLifetime;
    }

}
