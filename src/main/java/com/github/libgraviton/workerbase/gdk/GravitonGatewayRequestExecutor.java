package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.api.gateway.GravitonGateway;
import com.github.libgraviton.workerbase.gdk.requestexecutor.auth.Authenticator;

public class GravitonGatewayRequestExecutor extends RequestExecutor {

    public GravitonGatewayRequestExecutor(ObjectMapper objectMapper, GravitonGateway gateway, Authenticator authenticator) {
        super(objectMapper, gateway, authenticator);
    }
}
