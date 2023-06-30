package com.github.libgraviton.workerbase.healthcheck;

import com.github.libgraviton.workerbase.helper.RabbitMqMgmtClient;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.helper.WorkerUtil;

import java.io.IOException;
import java.net.URISyntaxException;

public class QueueWorkerHealthcheck {
  public static void main(String[] args) throws IOException, URISyntaxException {
    WorkerProperties.load();

    RabbitMqMgmtClient rabbitMqMgmtClient = new RabbitMqMgmtClient(
      WorkerProperties.QUEUE_HOST.get(),
      Integer.parseInt(WorkerProperties.QUEUE_MGMTPORT.get()),
      WorkerProperties.QUEUE_USER.get(),
      WorkerProperties.QUEUE_PASSWORD.get(),
      1
    );

    rabbitMqMgmtClient.ensureClientPresence(WorkerUtil.getQueueClientId());
  }
}
