package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;;
import com.github.libgraviton.workerbase.helper.QueueEventScope;

public interface AsyncQueueWorkerInterface {

  WorkerRunnableInterface handleRequestAsync(QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;

}
