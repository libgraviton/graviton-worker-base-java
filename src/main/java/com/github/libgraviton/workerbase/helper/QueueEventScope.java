package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import com.github.libgraviton.workerbase.model.QueueEvent;
import io.activej.inject.annotation.Provides;
import io.activej.inject.annotation.Transient;

import java.util.Map;
import java.util.Properties;

/**
 * there are things that need to be scoped differently in the scope of a *single* QueueEvent.
 * this class serves as a wrapper to get things correctly scoped..
 */
@GravitonWorkerDiScan
public class QueueEventScope extends WorkerScope {

    @Provides
    @Transient
    public static QueueEventScope getInstance(Properties properties, GravitonApi gravitonApi, EventStatusHandler statusHandler, GravitonFileEndpoint fileEndpoint) {
        return new QueueEventScope(properties, gravitonApi, statusHandler, fileEndpoint);
    }

    private QueueEvent queueEvent;

    private QueueEventScope(Properties properties, GravitonApi gravitonApi, EventStatusHandler statusHandler, GravitonFileEndpoint fileEndpoint) {
        super(properties, gravitonApi, statusHandler, fileEndpoint);
    }

    public void setQueueEvent(QueueEvent queueEvent) {
        this.queueEvent = queueEvent;
        Map<String, String> headers = Map.copyOf(queueEvent.getTransientHeaders());
        gravitonApi.setTransientHeaders(headers);
        fileEndpoint.gravitonApi().setTransientHeaders(headers);
        statusHandler.getGravitonApi().setTransientHeaders(headers);
    }

    public QueueEvent getQueueEvent() {
        return queueEvent;
    }
}
