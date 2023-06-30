package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadata;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadataAction;
import com.github.libgraviton.workerbase.lib.TestFileQueueWorker;
import com.github.libgraviton.workerbase.lib.TestFileQueueWorkerIgnoredAction;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workertestbase.WorkerTestExtension;
import com.github.tomakehurst.wiremock.matching.MultipartValuePatternBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class FileWorkerBaseTest {

    @RegisterExtension
    public static WorkerTestExtension workerTestExtension = (new WorkerTestExtension())
            .setStartWiremock(true)
            .setStartRabbitMq(true);

    @Test
    public void testFileHandling() throws Exception {

        File eventFile = new File();
        eventFile.setId("test-fileId");
        FileMetadata fileMetadata = new FileMetadata();
        FileMetadataAction action = new FileMetadataAction();
        action.setCommand("doYourStuff");
        fileMetadata.setAction(List.of(action));
        eventFile.setMetadata(fileMetadata);

        WorkerLauncher workerLauncher = workerTestExtension.getWrappedWorker(TestFileQueueWorker.class);

        final CountDownLatch countDownLatch = workerTestExtension.getCountDownLatch(1, workerLauncher);

        workerLauncher.run();

        Map<String, String> transientHeaders = Map.of("my-custom-header", "file-dude", "header", "value");
        QueueEvent queueEvent = workerTestExtension.getQueueEvent(transientHeaders, "USER_ID", eventFile);

        workerTestExtension.getWireMockServer().stubFor(
                put(urlEqualTo("/file/test-grv-file"))
                        .withHeader("my-custom-header", equalTo("file-dude"))
                        .withHeader("header", equalTo("value"))
                        .withMultipartRequestBody(new MultipartValuePatternBuilder().withName("upload").withName("metadata"))
                        .willReturn(aResponse().withStatus(201))
        );

        workerTestExtension.getWireMockServer().stubFor(
                get(urlEqualTo("/file/test-grv-file"))
                        .withHeader("my-custom-header", equalTo("file-dude"))
                        .withHeader("header", equalTo("value"))
                        .willReturn(aResponse().withBody("THIS-IS-THE-CONTENT").withHeader("Content-Type", "text/plain").withStatus(200))
        );

        // whatever request!
        workerTestExtension.getWireMockServer()
                .stubFor(
                        get(urlMatching("/core/app/hans"))
                                // the transient headers
                                .withHeader("my-custom-header", equalTo("file-dude"))
                                .withHeader("header", equalTo("value"))
                                .willReturn(
                                        aResponse().withBody("HANSITEST").withStatus(200)
                                )
                );

        workerTestExtension.sendToWorker(queueEvent);

        // wait until finished!
        countDownLatch.await();

        TestFileQueueWorker worker = (TestFileQueueWorker) workerLauncher.getWorker();

        Assertions.assertTrue(worker.onStartupCalled);
        Assertions.assertInstanceOf(File.class, worker.fileObj);

        workerTestExtension.getWireMockServer().verify(1,
                getRequestedFor(urlEqualTo("/core/app/hans"))
        );
    }

    @Test
    public void testFileHandlingActionIgnoredByBase() throws Exception {
        WorkerLauncher workerLauncher = workerTestExtension.getWrappedWorker(TestFileQueueWorkerIgnoredAction.class);
        final CountDownLatch countDownLatch = workerTestExtension.getCountDownLatch(1, workerLauncher);

        workerLauncher.run();

        File eventFile = new File();
        eventFile.setId("test-fileId");
        FileMetadata fileMetadata = new FileMetadata();
        FileMetadataAction action = new FileMetadataAction();
        action.setCommand("THIS-DOES-NOT-INTEREST-WORKER");
        fileMetadata.setAction(List.of(action));
        eventFile.setMetadata(fileMetadata);

        QueueEvent queueEvent = workerTestExtension.getQueueEvent(Map.of(), "HANS", eventFile);
        workerTestExtension.sendToWorker(queueEvent);

        countDownLatch.await();

        TestFileQueueWorkerIgnoredAction worker = (TestFileQueueWorkerIgnoredAction) workerLauncher.getWorker();

        Assertions.assertTrue(worker.onStartupCalled);
        Assertions.assertFalse(worker.handleFileRequestCalled);

        workerTestExtension.verifyQueueEventWasSetToStatus(queueEvent.getEvent(), "ignored");
        workerTestExtension.verifyQueueEventWasNotSetToStatus(queueEvent.getEvent(), "working");
        workerTestExtension.verifyQueueEventWasNotSetToStatus(queueEvent.getEvent(), "done");
    }
}
