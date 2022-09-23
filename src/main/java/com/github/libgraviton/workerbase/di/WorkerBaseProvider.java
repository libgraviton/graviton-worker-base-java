package com.github.libgraviton.workerbase.di;

import com.github.libgraviton.workerbase.QueueManager;
import com.github.libgraviton.workerbase.gdk.GravitonAuthApi;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import io.activej.inject.annotation.Provides;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerBaseProvider {

    @Provides
    public static Properties getProperties() throws IOException {
        return WorkerProperties.load();
    }

    @Provides
    public static QueueManager getQueueManager(Properties properties) {
        return new QueueManager(properties);
    }

    @Provides
    public static GravitonAuthApi getGravitonAuthApi(Properties properties) {
        return new GravitonAuthApi(properties);
    }

    @Provides
    public static ExecutorService executorService(Properties properties) {
        int size = Integer.parseInt(properties.getOrDefault("executor.threadPoolSize", "10").toString());
        return Executors.newFixedThreadPool(size);
    }

}
