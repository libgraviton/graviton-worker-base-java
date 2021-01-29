package com.github.libgraviton.workerbase.auth;

import com.github.libgraviton.gdk.api.Request;
import com.github.libgraviton.gdk.api.Response;
import com.github.libgraviton.workerbase.auth.exception.CannotProcessAuth;

/**
 * Abstract for auth strategies
 */
public abstract class Authenticator {

    protected String coreUserId;

    /**
     * returns the core user id
     *
     * @return user id
     */
    public String getCoreUserId() {
        return coreUserId;
    }

    /**
     * sets the current core user id
     *
     * @param currentCoreUserId user id
     */
    public void setCoreUserId(String currentCoreUserId) {
        coreUserId = currentCoreUserId;
    }

    /**
     * will be called before the actual request to give the auth strategy the possibility of
     * injecting headers or other things.
     *
     * @param request request
     * @return the altered request
     */
    public abstract Request.Builder beforeRequest(Request.Builder request) throws CannotProcessAuth;

    /**
     * will be called after the request. useful for cleanups..
     *
     * @param response response
     */
    public abstract void onResponse(Response response) throws CannotProcessAuth;

    /**
     * will be called in case of a request failure..
     */
    public abstract void onRequestFailure();

}
