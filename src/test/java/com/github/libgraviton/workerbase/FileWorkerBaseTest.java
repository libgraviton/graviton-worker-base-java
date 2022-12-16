package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadata;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadataAction;
import com.github.libgraviton.workerbase.lib.TestFileQueueWorker;
import com.github.libgraviton.workerbase.lib.WorkerTestExtension;
import com.github.libgraviton.workerbase.model.QueueEvent;
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

    /**
    private FileQueueWorkerAbstract sut;

    protected boolean innerWorkerWasCalled = false;

    @FunctionalInterface
    private interface FileHandler {
        void handleFileRequest(GravitonFileEndpoint fileEndpoint, QueueEvent body, File file) throws CommunicationException, MalformedURLException, URISyntaxException;
    }

    private FileQueueWorkerAbstract getFileTestWorker(FileHandler fileHandler) {
        WorkerScope workerScope = DependencyInjection.getInstance(WorkerScope.class);
        return new FileQueueWorkerAbstract(workerScope) {
            @Override
            public void onStartUp() throws WorkerException {

            }

            @Override
            public List<String> getActionsOfInterest(QueueEvent queueEvent) {
                return List.of("test");
            }

            @Override
            public void handleFileRequest(QueueEvent body, File file, QueueEventScope queueEventScope) throws WorkerException {
                try {
                    fileHandler.handleFileRequest(workerScope.getFileEndpoint(), body, file);
                    innerWorkerWasCalled = true;
                } catch (Throwable t) {
                    throw new WorkerException("Errror", t);
                }
            }
        };
    }
     **/

    /*
    TODO testing:
    * actions that are not in interested array
    * fetch file as binary (the content, not the file) in queueeventscope
    * assert all with the transient headers
     */

    @Test
    public void testFileHandling() throws Exception {

        File eventFile = new File();
        eventFile.setId("test-fileId");
        FileMetadata fileMetadata = new FileMetadata();
        FileMetadataAction action = new FileMetadataAction();
        action.setCommand("doYourStuff");
        fileMetadata.setAction(List.of(action));
        eventFile.setMetadata(fileMetadata);

        workerTestExtension.getWireMockServer().stubFor(
                put(urlEqualTo("/file/test-grv-file"))
                        .withMultipartRequestBody(new MultipartValuePatternBuilder().withName("upload").withName("metadata"))
                        .willReturn(aResponse().withStatus(201))
        );

        WorkerLauncher workerLauncher = workerTestExtension.getWrappedWorker(TestFileQueueWorker.class);

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        workerLauncher.getQueueWorkerRunner().addOnCompleteCallback((duration) -> {
            countDownLatch.countDown();
        });

        workerLauncher.run();

        QueueEvent queueEvent = workerTestExtension.getQueueEvent(Map.of(), "USER_ID", eventFile);

        workerTestExtension.sendToWorker(queueEvent);

        // wait until finished!
        countDownLatch.await();

        TestFileQueueWorker worker = (TestFileQueueWorker) workerLauncher.getWorker();

        Assertions.assertTrue(worker.onStartupCalled);
        Assertions.assertInstanceOf(File.class, worker.fileObj);

        int hans = 3;

        /*
        sut = getFileTestWorker((fileEndpoint, body, file) -> {
            // by comparing this we know that it was fetched through Wiremock and passed to the worker..
            // and by this we're testing FileEndpoint
            assertEquals(eventFile.getId(), file.getId());

            String data = "SPECIALINFORMATION";
            Request request = fileEndpoint.put(data.getBytes(StandardCharsets.UTF_8), file).build();
            List<Part> parts = request.getParts();
            assertEquals(2, parts.size());

            Part part1 = parts.get(1);
            assertEquals("upload", part1.getFormName());
            assertEquals(data, new String(part1.getBody()));

            Part part2 = parts.get(0);
            assertEquals("metadata", part2.getFormName());
            assertTrue(new String(part2.getBody()).contains("\"id\":\"test-fileId\""));

            // test real PUT
            URL testFileUrl = this.getClass().getClassLoader().getResource("files/test.pdf");
            java.io.File testFile = new java.io.File(testFileUrl.toURI());

            // testGrvFile
            File testGrvFile = new File();
            testGrvFile.setId("test-grv-file");

            fileEndpoint.put(testFile, testGrvFile).execute();
        });

         */

        /*
        prepareWorker(sut);

        produceQueueEvent(sut, eventFile);

        verify(1, getRequestedFor(urlMatching("^/file/(.*)")));
        verify(1, putRequestedFor(urlEqualTo("/file/test-grv-file")));

        assertTrue(innerWorkerWasCalled);

         */
    }

}
