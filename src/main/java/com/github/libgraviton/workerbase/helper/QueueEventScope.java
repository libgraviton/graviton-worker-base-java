/**
 * 
 */
package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.model.QueueEvent;
import io.activej.inject.annotation.Inject;

/**
 * there are things that need to be scoped differently in the scope of a *single* QueueEvent.
 * this class serves as a wrapper to get things correctly scoped..
 */
@GravitonWorkerDiScan
public class QueueEventScope {

    private final GravitonApi gravitonApi;
    private QueueEvent queueEvent;

    @Inject
    public QueueEventScope(GravitonApi gravitonApi) {
        this.gravitonApi = gravitonApi;
    }

    public void setQueueEvent(QueueEvent queueEvent) {
        this.queueEvent = queueEvent;
        gravitonApi.setTransientHeaders(queueEvent.getTransientHeaders());
    }

    public GravitonApi getGravitonApi() {
        return gravitonApi;
    }
}
