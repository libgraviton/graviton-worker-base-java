package com.github.libgraviton.workerbase.gdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.api.gateway.GravitonGateway;
import com.github.libgraviton.workerbase.gdk.requestexecutor.auth.Authenticator;
import com.github.libgraviton.workerbase.gdk.requestexecutor.auth.GravitonGatewayAuthenticator;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import io.activej.inject.annotation.Provides;

@GravitonWorkerDiScan
    public class GravitonGatewayRequestExecutor extends RequestExecutor {

    public GravitonGatewayRequestExecutor(ObjectMapper objectMapper, GravitonGateway gateway, Authenticator authenticator) {
        super(objectMapper, gateway, authenticator);
    }

    @Provides
    public static GravitonGatewayRequestExecutor getInstance(ObjectMapper objectMapper, GravitonGateway gateway) {
        Authenticator authenticator = new GravitonGatewayAuthenticator(
                WorkerProperties.GATEWAY_BASE_URL.get(),
                WorkerProperties.GATEWAY_USERNAME.get(),
                WorkerProperties.GATEWAY_PASSWORD.get()
        );

        return new GravitonGatewayRequestExecutor(objectMapper, gateway, authenticator);
    }
}
