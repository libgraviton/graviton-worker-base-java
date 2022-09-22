package com.github.libgraviton.workerbase.di;

import com.github.libgraviton.workerbase.QueueManager;
import com.github.libgraviton.workerbase.gdk.GravitonAuthApi;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import io.activej.inject.annotation.Provides;

import java.io.IOException;
import java.util.Properties;

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

}
