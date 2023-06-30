package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.lib.*;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workertestbase.WorkerTestExtension;
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
        final CountDownLatch countDownLatch = workerTestExtension.getCountDownLatch(7, workerLauncher);

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

        // test auto register on graviton
        workerTestExtension.getWireMockServer().verify(1,
                putRequestedFor(
                    urlEqualTo("/event/worker/" + WorkerProperties.WORKER_ID.get())
                )
                .withRequestBody(containing(WorkerProperties.WORKER_ID.get()))
        );

        // status working
        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent.getEvent(), "working");
        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent2.getEvent(), "working");

        // status done
        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent.getEvent(), "done");
        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent2.getEvent(), "done");

        // ignored
        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent3.getEvent(), "ignored");

        /* THIS EVENT FIRES AN EXCEPTION 3 TIMES AND 1 LAST TIME OK AND SET TO DONE */

        // 4 times to 'working'!
        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent4.getEvent(), "working", null, 4);

        // failed! -> we tried 3 times in the worker!
        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent4.getEvent(), "failed", "YES_I_DO_FAIL_NOW", 3);
        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent4.getEvent(), "done", null, 1);

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

        final CountDownLatch countDownLatch = workerTestExtension.getCountDownLatch(1, workerLauncher);

        workerLauncher.run();

        QueueEvent queueEvent = workerTestExtension.getQueueEvent();

        workerTestExtension.sendToWorker(queueEvent);

        // wait until finished!
        countDownLatch.await();

        // availability check
        workerTestExtension.getWireMockServer().verify(0, optionsRequestedFor(urlEqualTo("/")));

        // test auto register on graviton
        workerTestExtension.getWireMockServer().verify(0,
                putRequestedFor(
                        urlEqualTo("/event/worker/" + WorkerProperties.WORKER_ID.get())
                )
        );

        // 0 to status
        workerTestExtension.getWireMockServer().verify(0,
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
        ((TestQueueWorkerNoRetryOnException) workerLauncher.getWorker()).callCount = 0;

        final CountDownLatch countDownLatch = workerTestExtension.getCountDownLatch(1, workerLauncher);

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
     * case when worker is triggered with an EventStatus that DOES NOT exist!
     *
     * @throws Exception
     */
    @Test
    public void testBehaviorNonExistentQueueAction() throws Exception {
        WorkerLauncher workerLauncher = workerTestExtension.getWrappedWorker(TestQueueWorkerNoRetryOnException.class);
        ((TestQueueWorkerNoRetryOnException) workerLauncher.getWorker()).callCount = 0;

        final CountDownLatch countDownLatch = workerTestExtension.getCountDownLatch(1, workerLauncher);

        workerLauncher.run();

        QueueEvent queueEvent = workerTestExtension.getQueueEvent();
        // change id so it will lead to 404!
        String newId = queueEvent.getEvent()+"ANOTHER-ID";
        queueEvent.setEvent(newId);
        queueEvent.getStatus().set$ref(queueEvent.getStatus().get$ref()+"ANOTHER-ID");

        workerTestExtension.sendToWorker(queueEvent);

        // wait until finished!
        countDownLatch.await();

        TestQueueWorkerNoRetryOnException worker = (TestQueueWorkerNoRetryOnException) workerLauncher.getWorker();

        // should never have been called!
        Assertions.assertEquals(0, worker.callCount);

        int retryLimit = Integer.parseInt(WorkerProperties.STATUSHANDLER_RETRY_LIMIT.get());

        workerTestExtension.verifyQueueEventWasSetToStatus(newId, "working", null, retryLimit);

        try {
            workerLauncher.stop();
        } catch (Throwable t) {
            // something is wrong..
        }
    }
}
