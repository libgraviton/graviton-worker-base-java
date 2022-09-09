package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.model.QueueEvent;

public interface QueueWorkerInterface {

  /**
   * this is the first called when a queue event arrived - final in WorkerAbstract
   */
  void handleDelivery(QueueEvent queueEvent, String messageId, MessageAcknowledger acknowledger);

  /**
   * this is implemented in the worker itself.
   */
  void handleRequest(QueueEvent body) throws WorkerException, GravitonCommunicationException;

  /**
   * true or false - should we handle this request?
   */
  boolean shouldHandleRequest(QueueEvent body) throws WorkerException, GravitonCommunicationException;

  QueueManager getQueueManager();

  Boolean shouldAutoAcknowledgeOnException();

}
