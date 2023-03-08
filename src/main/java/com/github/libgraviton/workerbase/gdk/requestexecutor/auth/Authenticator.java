package com.github.libgraviton.workerbase.gdk.requestexecutor.auth;

import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.requestexecutor.exception.AuthenticatorException;

public interface Authenticator extends AutoCloseable {

    Request onRequest(Request request) throws AuthenticatorException;

}
