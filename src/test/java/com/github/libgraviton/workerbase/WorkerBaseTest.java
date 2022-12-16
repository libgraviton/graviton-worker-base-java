package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.lib.*;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WorkerBaseTest {

    @RegisterExtension
    public static WorkerTestExtension workerTestExtension = (new WorkerTestExtension())
            .setStartWiremock(true)
            .setStartRabbitMq(true);

    /**
     *
     * TODO TESTING
     * - 404 event status!
     * - FileWorkerTest!
     *
     * TODO REST
     * - GravitonGatewayAuthenticator
     * - DownloadClient
     *
     *
     * @throws Exception
     */

    @Test
    public void testAllTheBasicsAndAutoRegister() throws Exception {
        WorkerLauncher workerLauncher = workerTestExtension.getWrappedWorker(TestQueueWorker.class);

        // set these transient headers
        Map<String, String> transientHeaders = Map.of("my-custom-header", "has-this-value", "header", "value");
        Map<String, String> transientHeaders2 = Map.of("my-custom-header", "has-this-value2", "header", "value2");

        // register stub for files -> this ensures that transient headers will be applied! -> tests QueueEventScope!
        StubMapping transientHeaderStub = workerTestExtension.getWireMockServer()
                .stubFor(
                    get(urlMatching("/file/test-workerfile"))
                // the transient headers
                .withHeader("my-custom-header", equalTo("has-this-value"))
                .withHeader("header", equalTo("value"))
                .willReturn(
                        aResponse().withBodyFile("fileResource.json").withStatus(200)
                )
        );
        StubMapping transientHeaderStub2 = workerTestExtension.getWireMockServer()
                .stubFor(
                    get(urlMatching("/file/test-workerfile"))
                        // the transient headers
                        .withHeader("my-custom-header", equalTo("has-this-value2"))
                        .withHeader("header", equalTo("value2"))
                        .willReturn(
                                aResponse().withBodyFile("fileResource.json").withStatus(200)
                        )
        );

        // this is the way how we wait for queue handling to be finished!
        final CountDownLatch countDownLatch = new CountDownLatch(7);
        workerLauncher.getQueueWorkerRunner().addOnCompleteCallback((duration) -> {
            countDownLatch.countDown();
        });

        workerLauncher.run();

        QueueEvent queueEvent = workerTestExtension.getQueueEvent(transientHeaders);
        QueueEvent queueEvent2 = workerTestExtension.getQueueEvent(transientHeaders2);
        // this should be ignored!
        QueueEvent queueEvent3 = workerTestExtension.getQueueEvent(Map.of(), "SPECIAL_USER");
        // this should fail!
        QueueEvent queueEvent4 = workerTestExtension.getQueueEvent(transientHeaders, "PLEASE_FAIL_HERE!");

        workerTestExtension.sendToWorker(queueEvent);
        workerTestExtension.sendToWorker(queueEvent2);
        workerTestExtension.sendToWorker(queueEvent3);
        workerTestExtension.sendToWorker(queueEvent4);

        // wait until finished!
        countDownLatch.await();

        // availability check
        verify(moreThan(1), optionsRequestedFor(urlEqualTo("/")));

        // test auto register on graviton
        verify(1,
                putRequestedFor(
                    urlEqualTo("/event/worker/" + WorkerProperties.getProperty(WorkerProperties.WORKER_ID))
                )
                .withRequestBody(containing(WorkerProperties.getProperty(WorkerProperties.WORKER_ID)))
        );

        // status working
        verify(1,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent.getEvent()))
                .withRequestBody(containing("\"working\""))
        );
        verify(1,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent2.getEvent()))
                        .withRequestBody(containing("\"working\""))
        );
        // status done
        verify(1,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent.getEvent()))
                        .withRequestBody(containing("\"done\""))
        );
        verify(1,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent2.getEvent()))
                        .withRequestBody(containing("\"done\""))
        );
        // ignored
        verify(1,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent3.getEvent()))
                        .withRequestBody(containing("\"ignored\""))
        );

        /* THIS EVENT FIRES AN EXCEPTION 3 TIMES AND 1 LAST TIME OK AND SET TO DONE */

        // 4 times to 'working'!
        verify(4,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent4.getEvent()))
                        .withRequestBody(containing("\"working\""))
        );
        // failed! -> we tried 3 times in the worker!
        verify(3,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent4.getEvent()))
                        .withRequestBody(containing("\"failed\""))
                        .withRequestBody(containing("YES_I_DO_FAIL_NOW")) // error message included?
        );
        // 4th time this should be put to done!
        verify(1,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent4.getEvent()))
                        .withRequestBody(containing("\"done\""))
        );

        // FINISHED EVENT 4

        TestQueueWorker worker = (TestQueueWorker) workerLauncher.getWorker();

        // test whether the "transient headers" from the queueEvent were used correctly
        boolean fileWithTransientHeadersWasMatched = false;
        boolean fileWithTransientHeadersWasMatched2 = false;
        for (ServeEvent serveEvent : workerTestExtension.getWireMockServer().getAllServeEvents()) {
            if (serveEvent.getStubMapping().getId().equals(transientHeaderStub.getId())) {
                fileWithTransientHeadersWasMatched = true;
            }
            if (serveEvent.getStubMapping().getId().equals(transientHeaderStub2.getId())) {
                fileWithTransientHeadersWasMatched2 = true;
            }
        }

        Assertions.assertTrue(fileWithTransientHeadersWasMatched);
        Assertions.assertTrue(fileWithTransientHeadersWasMatched2);

        // assert onStartup has been called
        Assertions.assertTrue(worker.hasStartedUp);
        Assertions.assertTrue(worker.shouldHandleRequestCalled);
        Assertions.assertNotNull(worker.fetchedFile);
        Assertions.assertEquals(6, worker.callCount);
        Assertions.assertEquals(3, worker.errorCount);

        try {
            workerLauncher.stop();
        } catch (Throwable t) {
            // something is wrong..
        }
    }

    @Test
    public void testNoAutoRegisterAndStatusWorker() throws Exception {
        WorkerLauncher workerLauncher = workerTestExtension.getWrappedWorker(TestQueueWorkerNoAuto.class);

        // this is the way how we wait for queue handling to be finished!
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        workerLauncher.getQueueWorkerRunner().addOnCompleteCallback((duration) -> {
            countDownLatch.countDown();
        });

        workerLauncher.run();

        QueueEvent queueEvent = workerTestExtension.getQueueEvent();

        workerTestExtension.sendToWorker(queueEvent);

        // wait until finished!
        countDownLatch.await();

        // availability check
        verify(0, optionsRequestedFor(urlEqualTo("/")));

        // test auto register on graviton
        verify(0,
                putRequestedFor(
                        urlEqualTo("/event/worker/" + WorkerProperties.getProperty(WorkerProperties.WORKER_ID))
                )
        );

        // 0 to status
        verify(0,
                patchRequestedFor(urlEqualTo("/event/status/" + queueEvent.getEvent()))
        );

        TestQueueWorkerNoAuto worker = (TestQueueWorkerNoAuto) workerLauncher.getWorker();

        Assertions.assertTrue(worker.handleRequestCalled);
        Assertions.assertTrue(worker.concerningRequestCalled);

        try {
            workerLauncher.stop();
        } catch (Throwable t) {
            // something is wrong..
        }
    }

    @Test
    public void testAutoAckOnException() throws Exception {
        WorkerLauncher workerLauncher = workerTestExtension.getWrappedWorker(TestQueueWorkerNoRetryOnException.class);

        // this is the way how we wait for queue handling to be finished!
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        workerLauncher.getQueueWorkerRunner().addOnCompleteCallback((duration) -> {
            countDownLatch.countDown();
        });

        workerLauncher.run();

        QueueEvent queueEvent = workerTestExtension.getQueueEvent();

        workerTestExtension.sendToWorker(queueEvent);

        // wait until finished!
        countDownLatch.await();

        TestQueueWorkerNoRetryOnException worker = (TestQueueWorkerNoRetryOnException) workerLauncher.getWorker();

        // only once as worker opted to autoAck on Exception!
        Assertions.assertEquals(1, worker.callCount);

        try {
            workerLauncher.stop();
        } catch (Throwable t) {
            // something is wrong..
        }
    }

    /**
    @Test
    public void testGetWorkerAction() throws Exception {
        when(gravitonApi
                .getEndpointManager()
                .getEndpoint(EventStatusAction.class.getName())
                .getUrl())
                .thenReturn("http://localhost:8000/event/action/");

        TestQueueWorker testWorker = prepareTestWorker(new TestQueueWorker());
        // to initialize worker
        worker = getWrappedWorker(testWorker);
        EventStatusStatusAction actionRef = testWorker.getWorkerAction();

        assertEquals("http://localhost:8000/event/action/java-test-default", actionRef.get$ref());
    }


    @Test
    public void testStatusUpdateOnShouldNotHandleRequest() throws Exception {
        TestQueueWorker testWorker = spy(prepareTestWorker(new TestQueueWorker()));
        doReturn(false).when(testWorker).shouldHandleRequest(any(QueueEvent.class));
        worker = getWrappedWorker(testWorker);
        worker.run();

        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), StandardCharsets.UTF_8);
        workerConsumer.consume("34343", message);

        verify(testWorker, times(1)).shouldHandleRequest(any(QueueEvent.class));

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        // check if event status will be fetched before every update
        verify(gravitonApi, times(2)).get(anyString());
        // ignored
        verify(gravitonApi, times(1)).patch(isA(EventStatus.class));
    }

    @Test
    public void testWorkerException() throws Exception {
        TestQueueWorkerException testWorker = prepareTestWorker(new TestQueueWorkerException());
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), StandardCharsets.UTF_8);
        workerConsumer.consume("34343", message);

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        // check if event status will be fetched before every update
        verify(gravitonApi, times(3)).get(anyString());
        // working update & failed update
        verify(gravitonApi, times(2)).patch(isA(EventStatus.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBackendStatusUpdateError() throws Exception {
        
        // change mocking so we get an unsuccessful response on /event/status update -> worker shall throw GravitonCommunicationException
        when(gravitonApi.patch(any(EventStatus.class)).execute()).thenThrow(UnsuccessfulResponseException.class);
        
        TestQueueWorker testWorker = prepareTestWorker(new TestQueueWorker());
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw CommunicationException
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), StandardCharsets.UTF_8);
        workerConsumer.consume("34343", message);
    }

    private <T extends QueueWorkerAbstract> T prepareTestWorker(T worker) {

        DependencyInjection.init(List.of());
        DependencyInjection.addInstanceOverride(WorkerInterface.class, worker);
        DependencyInjection.addInstanceOverride(GravitonApi.class, gravitonApi);
        DependencyInjection.addInstanceOverride(EventStatusHandler.class, new EventStatusHandler(gravitonApi));


        return worker;
    }
    */
}
