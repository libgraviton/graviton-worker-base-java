package com.github.libgraviton.workerbase.gdk.requestexecutor.auth;

import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.requestexecutor.exception.AuthenticatorException;

public interface Authenticator {

    public Request onRequest(Request request) throws AuthenticatorException;

    public void onClose() throws AuthenticatorException;

}
