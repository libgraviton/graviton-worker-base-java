package com.github.libgraviton.workerbase.di;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.QueueManager;
import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import com.github.libgraviton.workerbase.gdk.api.endpoint.GeneratedEndpointManager;
import com.github.libgraviton.workerbase.gdk.api.endpoint.exception.UnableToLoadEndpointAssociationsException;
import com.github.libgraviton.workerbase.gdk.api.gateway.GravitonGateway;
import com.github.libgraviton.workerbase.gdk.api.gateway.OkHttpGateway;
import com.github.libgraviton.workerbase.gdk.api.gateway.okhttp.OkHttpGatewayFactory;
import com.github.libgraviton.workerbase.gdk.serialization.mapper.RqlObjectMapper;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import io.activej.inject.Key;
import io.activej.inject.annotation.Provides;
import io.activej.inject.annotation.Transient;
import io.activej.inject.binding.Binding;
import io.activej.inject.module.AbstractModule;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerBaseProvider extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerBaseProvider.class);

    @Override
    protected void configure() {
        /*
         * set our GravitonGateway to use
         */
        generate(GravitonGateway.class, (bindings, scope, key) -> {
            if (key.getType().equals(GravitonGateway.class)) {
                return Binding.to(Key.of(OkHttpGateway.class));
            }
            return null;
        });
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
    public static EventStatusHandler eventStatusHandler(GravitonApi gravitonApi) {
        return new EventStatusHandler(gravitonApi);
    }

    @Provides
    public static OkHttpClient getOkHttpClient(Properties properties) throws Exception {
        boolean hasRetry = properties.getProperty("graviton.okhttp.shouldRetry").equals("true");
        boolean forceHttp11 = properties.getProperty("graviton.okhttp.forcehttp11").equals("true");
        boolean trustAll = properties.getProperty("graviton.okhttp.trustAll").equals("true");

        OkHttpClient client = OkHttpGatewayFactory.getInstance(hasRetry);
        if (trustAll) {
            client = OkHttpGatewayFactory.getAllTrustingInstance(hasRetry, client);
        }

        if (forceHttp11) {
            client = client
                    .newBuilder()
                    .retryOnConnectionFailure(true)
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .build();
        }

        return client;
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
        int size = Integer.parseInt(properties.getOrDefault("executor.threadPoolSize", "10").toString());
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
