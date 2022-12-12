package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.model.QueueEvent;

import java.util.Properties;

public abstract class AsyncQueueWorkerAbstract extends QueueWorkerAbstract implements AsyncQueueWorkerInterface {

    public AsyncQueueWorkerAbstract(WorkerScope workerScope) {
        super(workerScope);
    }

    @Override
    final public void handleRequest(QueueEvent body, QueueEventScope queueEventScope) {
    }
}
