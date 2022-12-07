package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.model.QueueEvent;

public abstract class AsyncQueueWorkerAbstract extends QueueWorkerAbstract implements AsyncQueueWorkerInterface {
    @Override
    final public void handleRequest(QueueEvent body, GravitonApi gravitonApi) {
    }
}
