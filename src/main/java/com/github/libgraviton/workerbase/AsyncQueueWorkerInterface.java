package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;;
import com.github.libgraviton.workerbase.model.QueueEvent;

public interface AsyncQueueWorkerInterface {

  WorkerRunnableInterface handleRequestAsync(QueueEvent body) throws WorkerException, GravitonCommunicationException;

}
