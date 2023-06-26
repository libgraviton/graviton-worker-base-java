package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.util.RetryRegistry;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.domain.ConnectionInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMqMgmtClient {

  private final Logger LOG = LoggerFactory.getLogger(RabbitMqMgmtClient.class);
  private final Client client;
  private final int maxTries;

  public RabbitMqMgmtClient(String queueHost, int managementPort, String username, String password, int maxTries) throws MalformedURLException, URISyntaxException {
    String clientUrl = String.format(
      "http://%s:%s/api",
      queueHost,
      managementPort
    );
    this.maxTries = maxTries;
    client = new Client(
      new ClientParameters().url(clientUrl).username(username).password(password));
  }

  public void ensureClientPresence(String clientName) throws IOException {
    ensureClientPresence(List.of(clientName), false);
  }

  public void ensureClientPresence(List<String> clientNames) throws IOException {
    ensureClientPresence(clientNames, false);
  }

  public void ensureClientPresence(List<String> clientNames, boolean silentCheck) throws IOException {
    try {
      RetryRegistry.retrySomething(
        maxTries,
        () -> {
          if (!checkClientPresence(clientNames)) {
            throw new IOException("Could not detect all needed clients.");
          }
          return null;
        },
        (event) -> {
          if (!silentCheck) {
            LOG.warn("Error on checking client presence: {}", event.getLastThrowable() == null ? "?" : event.getLastThrowable().getMessage());
          }
        }
      );
    } catch (Throwable e) {
      if (!silentCheck) {
        LOG.error(
          "Retries exhausted for checking queue client presence.",
          e
        );
      }
      throw new IOException("Retries exhausted for checking queue client presence.", e);
    }
  }

  private boolean checkClientPresence(List<String> checkNames) {
    List<String> found = new ArrayList<>();

    for (ConnectionInfo connectionInfo : client.getConnections()) {
      if (connectionInfo.getClientProperties().getConnectionName() != null && checkNames.contains(connectionInfo.getClientProperties().getConnectionName())) {
        found.add(connectionInfo.getClientProperties().getConnectionName());
      }
    }

    return found.size() == checkNames.size();
  }
}