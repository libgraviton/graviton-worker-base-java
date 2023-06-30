package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.lib.TestAsyncQueueWorker;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workertestbase.WorkerTestExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class AsyncWorkerBaseTest {

    @RegisterExtension
    public static WorkerTestExtension workerTestExtension = (new WorkerTestExtension())
            .setStartWiremock(true)
            .setStartRabbitMq(true);

    @Test
    public void testBasicAsyncHandling() throws Throwable {
        WorkerLauncher workerLauncher = workerTestExtension.getWrappedWorker(TestAsyncQueueWorker.class);

        // generate 30 events!
        List<QueueEvent> events = new ArrayList<>();
        int counter = 0;

        while (counter < 30) {
            Map<String, String> transientHeaders = Map.of("EVENT-NUMBER", "NUMBER-"+counter);

            File file = new File();
            file.setId("NUMBER-"+counter);

            QueueEvent queueEvent = workerTestExtension.getQueueEvent(transientHeaders, "EVENT-NUMBER-"+ counter, file);

            events.add(queueEvent);
            counter++;
        }

        // this is the way how we wait for queue handling to be finished!
        final CountDownLatch countDownLatch = workerTestExtension.getCountDownLatch(events.size(), workerLauncher);

        workerLauncher.run();

        for (QueueEvent event : events) {
            workerTestExtension.sendToWorker(event);
        }

        // wait until finished!
        countDownLatch.await();

        Thread.sleep(1000);

        TestAsyncQueueWorker worker = (TestAsyncQueueWorker) workerLauncher.getWorker();

        Assertions.assertEquals(30, worker.shouldHandleCallCount.get());
        Assertions.assertEquals(60, worker.handleRequestCallCount.get());
        Assertions.assertTrue(worker.onStartupCalled);

        for (QueueEvent event : events) {
            workerTestExtension.verifyQueueEventWasDone(event.getEvent());
        }

        try {
            workerLauncher.stop();
        } catch (Throwable t) {
            // something is wrong..
        }
    }
}
