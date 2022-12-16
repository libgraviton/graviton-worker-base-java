package com.github.libgraviton.workerbase.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.gdk.data.GravitonBase;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.QueueManager;
import com.github.libgraviton.workerbase.WorkerInterface;
import com.github.libgraviton.workerbase.WorkerLauncher;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.messaging.exception.CannotPublishMessage;
import com.github.libgraviton.workerbase.model.GravitonRef;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workertestbase.TestUtils;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Body;
import org.junit.jupiter.api.extension.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.RabbitMQContainer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class WorkerTestExtension implements
        BeforeEachCallback,
        AfterEachCallback,
        BeforeAllCallback,
        AfterAllCallback {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerTestExtension.class);
    private boolean startWiremock = false;
    private boolean startRabbitMq = false;

    protected static ObjectMapper objectMapper;

    protected static WireMockServer wireMockServer;
    protected static RabbitMQContainer rabbitMQContainer;
    protected static QueueManager queueManager;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        WorkerProperties.load();
        DependencyInjection.init();

        objectMapper = DependencyInjection.getInstance(ObjectMapper.class);

        if (startWiremock) {
            startWiremock();
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (startRabbitMq) {
            startRabbitMq();
            queueManager = DependencyInjection.getInstance(QueueManager.class);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        WorkerProperties.clearOverrides();

        if (rabbitMQContainer != null) {
            rabbitMQContainer.stop();
            rabbitMQContainer = null;
        }
        if (wireMockServer != null) {
            resetWiremock();
        }
    }

    public WorkerTestExtension setStartWiremock(boolean startWiremock) {
        this.startWiremock = startWiremock;
        return this;
    }

    public WorkerTestExtension setStartRabbitMq(boolean startRabbitMq) {
        this.startRabbitMq = startRabbitMq;
        return this;
    }

    public WireMockServer getWireMockServer() {
        return wireMockServer;
    }

    public QueueEvent getQueueEvent() throws JsonProcessingException {
        return getQueueEvent(Map.of(), null, null);
    }

    public QueueEvent getQueueEvent(Map<String, String> transientHeaders) throws JsonProcessingException {
        return getQueueEvent(transientHeaders, null, null);
    }

    public QueueEvent getQueueEvent(Map<String, String> transientHeaders, String coreUserId) throws JsonProcessingException {
        return getQueueEvent(transientHeaders, coreUserId, null);
    }

    public QueueEvent getQueueEvent(Map<String, String> transientHeaders, String coreUserId, GravitonBase returnObject) throws JsonProcessingException {
        String id = TestUtils.getRandomString();
        QueueEvent queueEvent = new QueueEvent();
        queueEvent.setCoreUserId(coreUserId);
        queueEvent.setTransientHeaders(transientHeaders);
        queueEvent.setEvent(id);

        // ref!
        if (returnObject != null) {
            boolean isFile = (returnObject instanceof File);

            // wire returnObject to document
            String docUrl;
            if (!isFile) {
                docUrl = "/documents/" + returnObject.getId();
            } else {
                docUrl = "/file/" + returnObject.getId();
            }

            GravitonRef documentRef = new GravitonRef();
            documentRef.set$ref(WorkerProperties.getProperty(WorkerProperties.GRAVITON_BASE_URL) + docUrl);

            queueEvent.setDocument(documentRef);

            wireMockServer.stubFor(get(urlEqualTo(docUrl)).withHeader("Accept", containing("application/json"))
                    .willReturn(
                            aResponse().withStatus(200).withResponseBody(new Body(objectMapper.writeValueAsString(returnObject)))
                    )
            );
            wireMockServer.stubFor(patch(urlEqualTo(docUrl))
                    .willReturn(
                            aResponse().withStatus(201)
                    )
            );
        }

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

    public WorkerLauncher getWrappedWorker(Class<? extends WorkerInterface> clazz) throws Exception {
        Properties properties = DependencyInjection.getInstance(Properties.class);
        WorkerInterface worker = DependencyInjection.getInstance(clazz);

        return new WorkerLauncher(
                worker,
                properties
        );
    }


    public void sendToWorker(QueueEvent queueEvent) throws JsonProcessingException, CannotPublishMessage {
        queueManager.publish(objectMapper.writeValueAsString(queueEvent));
    }

    private void resetWiremock() {
        wireMockServer.resetAll();
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

    private void startWiremock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080)); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();
        resetWiremock();
    }

    private void startRabbitMq() {
        rabbitMQContainer = new RabbitMQContainer("rabbitmq:3-management").withAdminPassword(null);
        rabbitMQContainer.start();

        WorkerProperties.setOverride("queue.port", String.valueOf(rabbitMQContainer.getAmqpPort()));
    }

}
