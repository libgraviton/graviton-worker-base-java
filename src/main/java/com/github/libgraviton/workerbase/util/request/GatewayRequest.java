package com.github.libgraviton.workerbase.util.request;

import com.github.libgraviton.workerbase.gdk.GravitonGatewayRequestExecutor;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulResponseException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;


public class GatewayRequest extends RequestBase {

    public GatewayRequest() {
        super(WorkerProperties.getProperty("gateway.url"));
    }

    public GatewayRequest(@NotNull String route) {
        super(WorkerProperties.getProperty("gateway.url"), route);
    }

    public @NotNull Response execute() throws UnsuccessfulResponseException, MalformedURLException, CommunicationException {
        try (GravitonGatewayRequestExecutor requestExecutor = DependencyInjection.getInstance(GravitonGatewayRequestExecutor.class)) {
            return requestExecutor.execute(createBuilder().build());
        }
    }
}