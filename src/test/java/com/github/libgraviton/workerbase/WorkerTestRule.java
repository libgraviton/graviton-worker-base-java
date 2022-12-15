package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.messaging.exception.CannotPublishMessage;
import com.github.libgraviton.workerbase.model.GravitonRef;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workertestbase.TestUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Body;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class WorkerTestRule implements TestRule {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerTestRule.class);

    protected static ObjectMapper objectMapper;

    protected static WireMockServer wireMockServer;
    protected static RabbitMQContainer rabbitMQContainer;

    protected static QueueManager queueManager;

    public WireMockServer getWireMockServer() {
        return wireMockServer;
    }

    public static QueueEvent getQueueEvent() throws JsonProcessingException {
        return getQueueEvent(Map.of());
    }

    public static QueueEvent getQueueEvent(Map<String, String> transientHeaders) throws JsonProcessingException {
        String id = TestUtils.getRandomString();
        QueueEvent queueEvent = new QueueEvent();
        queueEvent.setTransientHeaders(transientHeaders);
        queueEvent.setEvent(id);

        // setup eventstatus
        EventStatus eventStatus = new EventStatus();
        eventStatus.setEventName("testevent");
        eventStatus.setId(id);

        EventStatusStatus eventStatusStatus = new EventStatusStatus();
        eventStatusStatus.setStatus(EventStatusStatus.Status.OPENED);
        eventStatusStatus.setWorkerId(WorkerProperties.getProperty(WorkerProperties.WORKER_ID));

        eventStatus.setStatus(List.of(eventStatusStatus));

        String eventStatusUrl = WorkerProperties.getProperty(WorkerProperties.GRAVITON_BASE_URL) + "/event/status/" + id;

        LOG.info("********* EVENT STATUS URL {}", eventStatusUrl);

        wireMockServer.stubFor(get(urlEqualTo("/event/status/" + id))
                .willReturn(
                        aResponse().withStatus(200).withResponseBody(new Body(objectMapper.writeValueAsString(eventStatus)))
                )
                .atPriority(100)
        );

        // status patches
        wireMockServer.stubFor(patch(urlEqualTo("/event/status/" + id))
                .willReturn(
                        aResponse().withStatus(200)
                )
                .atPriority(100)
        );

        GravitonRef ref = new GravitonRef();
        ref.set$ref(eventStatusUrl);

        queueEvent.setStatus(ref);

        return queueEvent;
    }

    public static void sendToWorker(QueueEvent queueEvent) throws JsonProcessingException, CannotPublishMessage {
        queueManager.publish(objectMapper.writeValueAsString(queueEvent));
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {

                List<Throwable> errors = new ArrayList<>();

                try {
                    WorkerProperties.load();
                    DependencyInjection.init(List.of());

                    startWiremock();
                    startRabbitMq();

                    objectMapper = DependencyInjection.getInstance(ObjectMapper.class);
                    queueManager = DependencyInjection.getInstance(QueueManager.class);

                    LOG.info("Running test {}...", description.getDisplayName());
                    statement.evaluate();
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    finished(description);
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };

    }

    protected void finished(Description description) {
        LOG.info("Entering test shutdown...");
        if (wireMockServer != null) {
            wireMockServer.resetAll();
            wireMockServer.stop();
        }
        if (rabbitMQContainer != null) {
            rabbitMQContainer.stop();
            rabbitMQContainer = null;
        }
    }

    private void startWiremock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080)); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();

        wireMockServer.stubFor(options(urlEqualTo("/"))
                .willReturn(
                        aResponse().withStatus(200)
                )
                .atPriority(Integer.MAX_VALUE)
        );

        wireMockServer.stubFor(post(urlEqualTo("/event/worker"))
                .willReturn(
                        aResponse().withStatus(201)
                )
                .atPriority(Integer.MAX_VALUE)
        );

        wireMockServer.stubFor(put(urlMatching("/event/worker/(.*)"))
                .willReturn(
                        aResponse().withStatus(201)
                )
                .atPriority(Integer.MAX_VALUE)
        );

        wireMockServer.stubFor(get(urlMatching("/event/status/(.*)"))
                .willReturn(
                        aResponse().withBodyFile("eventStatusResponse.json").withStatus(200)
                )
                .atPriority(Integer.MAX_VALUE)
        );
    }

    private void startRabbitMq() {
        rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management").withAdminPassword(null);
        rabbitMQContainer.start();

        WorkerProperties.setProperty("queue.port", String.valueOf(rabbitMQContainer.getAmqpPort()));
    }


}
