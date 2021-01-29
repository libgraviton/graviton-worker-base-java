package com.github.libgraviton.workerbase.auth;

import com.github.libgraviton.gdk.api.Request.Builder;
import com.github.libgraviton.gdk.api.Response;


/**
 * dummy authenticator.
 */
public class DummyAuth extends Authenticator {

    /**
     * constructor
     */
    public DummyAuth() {
        setCoreUserId("dummy-auth-core-user-id");
    }

    /**
     * @see Authenticator#beforeRequest(Builder)
     */
    @Override
    public Builder beforeRequest(Builder request) {
        return request;
    }

    /**
     * @see Authenticator#onResponse(Response)
     */
    @Override
    public void onResponse(Response response) {
    }

    /**
     * @see Authenticator#onRequestFailure()
     */
    @Override
    public void onRequestFailure() {
    }
}
