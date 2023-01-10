package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;

@FunctionalInterface
public interface WorkerRunnableInterface {
    void doWork(QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;
}
