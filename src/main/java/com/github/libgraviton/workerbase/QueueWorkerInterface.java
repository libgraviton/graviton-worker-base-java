package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.model.QueueEvent;

public interface QueueWorkerInterface {
  /**
   * this is implemented in the worker itself.
   */
  void handleRequest(QueueEvent body, QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;

  /**
   * true or false - should we handle this request?
   */
  boolean shouldHandleRequest(QueueEvent body) throws WorkerException, GravitonCommunicationException;

  boolean shouldAutoAcknowledgeOnException();

}
