package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;

public interface QueueWorkerInterface {
  /**
   * this is implemented in the worker itself.
   */
  void handleRequest(QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;

  /**
   * true or false - should we handle this request?
   */
  boolean shouldHandleRequest(QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;

  boolean shouldAutoAcknowledgeOnException();

}
