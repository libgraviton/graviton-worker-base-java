package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;

public abstract class AsyncQueueWorkerAbstract extends QueueWorkerAbstract implements AsyncQueueWorkerInterface {

    public AsyncQueueWorkerAbstract(WorkerScope workerScope) {
        super(workerScope);
    }

    @Override
    final public void handleRequest(QueueEventScope queueEventScope) {
    }
}
