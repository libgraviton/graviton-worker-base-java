package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorkerSubscription;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;

import java.util.List;

public interface QueueWorkerInterface {
  /**
   * this is implemented in the worker itself.
   */
  void handleRequest(QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;

  /**
   * true or false - should we handle this request?
   */
  boolean shouldHandleRequest(QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException;

  boolean shouldLinkAction(String workerId, List<EventStatusStatus> status);

  List<EventWorkerSubscription> getSubscriptions();

  boolean shouldAutoAcknowledgeOnException();

}
