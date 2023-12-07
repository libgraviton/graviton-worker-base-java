package com.github.libgraviton.workerbase.di;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.QueueManager;
import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import com.github.libgraviton.workerbase.gdk.GravitonGatewayRequestExecutor;
import com.github.libgraviton.workerbase.gdk.RequestExecutor;
import com.github.libgraviton.workerbase.gdk.api.endpoint.EndpointManager;
import com.github.libgraviton.workerbase.gdk.api.endpoint.GeneratedEndpointManager;
import com.github.libgraviton.workerbase.gdk.api.endpoint.exception.UnableToLoadEndpointAssociationsException;
import com.github.libgraviton.workerbase.gdk.api.gateway.GravitonGateway;
import com.github.libgraviton.workerbase.gdk.api.gateway.MethanolGateway;
import com.github.libgraviton.workerbase.gdk.api.gateway.http.MethanolGatewayFactory;
import com.github.libgraviton.workerbase.gdk.requestexecutor.auth.Authenticator;
import com.github.libgraviton.workerbase.gdk.requestexecutor.auth.GravitonGatewayAuthenticator;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.RqlObjectMapper;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.mizosoft.methanol.Methanol;
import io.activej.inject.Key;
import io.activej.inject.annotation.Provides;
import io.activej.inject.annotation.Transient;
import io.activej.inject.binding.Binding;
import io.activej.inject.module.AbstractModule;

import java.io.IOException;
import java.net.http.HttpClient;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.*;

public class WorkerBaseProvider extends AbstractModule {

    @Override
    protected void configure() {
        /*
         * set our GravitonGateway to use
         */
        generate(GravitonGateway.class, (bindings, scope, key) -> {
            if (key.getType().equals(GravitonGateway.class)) {
                return Binding.to(Key.of(MethanolGateway.class));
            }
            return null;
        });

        bind(EndpointManager.class).to(Key.of(GeneratedEndpointManager.class));
    }

    @Provides
    public static Properties getProperties() throws IOException {
        return WorkerProperties.load();
    }

    @Provides
    public static QueueManager getQueueManager(Properties properties) {
        return new QueueManager(properties);
    }

    @Provides
    @Transient
    public static EventStatusHandler eventStatusHandler(Properties properties, GravitonApi gravitonApi) {
        return new EventStatusHandler(
                gravitonApi,
                WorkerProperties.WORKER_ID.get(),
                Integer.parseInt(WorkerProperties.STATUSHANDLER_RETRY_LIMIT.get())
        );
    }

    @Provides
    @Transient
    public static GravitonApi gravitonApi(EndpointManager endpointManager, ObjectMapper objectMapper, RqlObjectMapper rqlObjectMapper) {
        return new GravitonApi(endpointManager, objectMapper, rqlObjectMapper);
    }

    @Provides
    public static Methanol getMethanol() throws Exception {
        final boolean hasRetry = WorkerProperties.HTTP_CLIENT_DORETRY.get().equals("true");
        final boolean trustAll = WorkerProperties.HTTP_CLIENT_TLS_TRUST_ALL.get().equals("true");

        return MethanolGatewayFactory.getInstance(hasRetry, trustAll, false);
    }

    @Provides
    public static HttpClient getHttpClient() throws Exception {
        return getMethanol();
    }

    @Provides
    public static RequestExecutor requestExecutor(ObjectMapper objectMapper, GravitonGateway gateway) {
        return new RequestExecutor(objectMapper, gateway);
    }

    /**
     * this needs to be transient as it has local state (the jwt token)!
     */
    @Provides
    @Transient
    public static GravitonGatewayRequestExecutor gravitonGatewayRequestExecutor(ObjectMapper objectMapper, GravitonGateway gateway) {
        Authenticator authenticator = new GravitonGatewayAuthenticator(
                WorkerProperties.GATEWAY_BASE_URL.get(),
                WorkerProperties.GATEWAY_USERNAME.get(),
                WorkerProperties.GATEWAY_PASSWORD.get()
        );

        return new GravitonGatewayRequestExecutor(objectMapper, gateway, authenticator);
    }

    @Provides
    public static GeneratedEndpointManager getGeneratedEndpointManager() throws UnableToLoadEndpointAssociationsException {
        return new GeneratedEndpointManager();
    }

    @Provides
    @Transient
    public static GravitonFileEndpoint getGravitonFileEndpoint(GravitonApi gravitonAuthApi) {
        return new GravitonFileEndpoint(gravitonAuthApi);
    }

    @Provides
    public static ExecutorService executorService(Properties properties) {
        int size = Integer.parseInt(WorkerProperties.THREADPOOL_SIZE.get());
        return Executors.newFixedThreadPool(size);
    }

    @Provides
    public static ObjectMapper objectMapper(Properties properties) {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat dateFormat = new SimpleDateFormat(properties.getProperty("graviton.date.format"));
        dateFormat.setTimeZone(TimeZone.getTimeZone(properties.getProperty("graviton.timezone")));
        objectMapper.setDateFormat(dateFormat);
        return objectMapper;
    }

    @Provides
    public static RqlObjectMapper rqlObjectMapper(Properties properties) {
        RqlObjectMapper objectMapper = new RqlObjectMapper();
        SimpleDateFormat dateFormat = new SimpleDateFormat(properties.getProperty("graviton.rql.date.format"));
        dateFormat.setTimeZone(TimeZone.getTimeZone(properties.getProperty("graviton.timezone")));
        objectMapper.setDateFormat(dateFormat);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }
}
