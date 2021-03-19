package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.model.QueueEvent;

public interface QueueWorkerInterface {

  void handleDelivery(QueueEvent queueEvent, String messageId, MessageAcknowledger acknowledger);

  QueueManager getQueueManager();

  Boolean shouldAutoAcknowledgeOnException();

}
