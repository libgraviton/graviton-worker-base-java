package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.exception.UnsuccessfulResponseException;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventstatusaction.document.EventStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorker;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorkerSubscription;
import com.github.libgraviton.workerbase.lib.TestWorker;
import com.github.libgraviton.workerbase.lib.TestWorkerException;
import com.github.libgraviton.workerbase.lib.TestWorkerNoAuto;
import com.github.libgraviton.workerbase.model.QueueEvent;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class})
public class WorkerBaseTest extends WorkerBaseTestCase {

    @Before
    public void setUp() throws Exception {    
        baseMock();
    }

    @Test
    public void testRegistrationPreparation() throws Exception {
        TestWorker testWorker = prepareTestWorker(new TestWorker());
        // to initialize worker
        worker = getWrappedWorker(testWorker);
        List<EventWorkerSubscription> subscriptions = testWorker.getSubscriptions();
        assertEquals(1, subscriptions.size());

        assertEquals("document.core.app.*", subscriptions.get(0).getEvent());
    }

    @Test
    public void testGetWorkerAction() throws Exception {
        when(gravitonApi
                .getEndpointManager()
                .getEndpoint(EventStatusAction.class.getName())
                .getUrl())
                .thenReturn("http://localhost:8000/event/action/");


        TestWorker testWorker = prepareTestWorker(new TestWorker());
        // to initialize worker
        worker = getWrappedWorker(testWorker);
        EventStatusStatusAction actionRef = testWorker.getWorkerAction();

        assertEquals("http://localhost:8000/event/action/java-test-default", actionRef.get$ref());
    }

    @Test
    public void testBasicExecution() throws Exception {
        TestWorker testWorker = prepareTestWorker(new TestWorker());
        worker = getWrappedWorker(testWorker);
        // execution should work even if message queue ack is not successful
        doThrow(new IOException()).when(queueChannel).basicAck(any(Long.class), any(Boolean.class));
        worker.run();

        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);

        assertTrue(testWorker.shouldHandleRequestCalled);

        QueueEvent queueEvent = testWorker.getHandledQueueEvent();
        assertEquals("documents.core.app.create", queueEvent.getEvent());
        assertEquals("http://localhost/core/app/admin", queueEvent.getDocument().get$ref());
        assertEquals("http://localhost/event/status/mystatus", queueEvent.getStatus().get$ref());

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        // check if event status will be fetched before every update
        verify(gravitonApi, times(3)).get(anyString());
        // working & done
        verify(gravitonApi, times(2)).patch(isA(EventStatus.class));
    }

    @Test
    public void testStatusUpdateOnShouldNotHandleRequest() throws Exception {
        TestWorker testWorker = spy(prepareTestWorker(new TestWorker()));
        doReturn(false).when(testWorker).shouldHandleRequest(any(QueueEvent.class));
        worker = getWrappedWorker(testWorker);
        worker.run();

        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
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
    public void testNoAutoWorker() throws Exception {
        TestWorkerNoAuto testWorker = spy(prepareTestWorker(new TestWorkerNoAuto()));
        worker = getWrappedWorker(testWorker);
        worker.run();

        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);
        
        assertTrue(testWorker.concerningRequestCalled);
        assertTrue(testWorker.handleRequestCalled);

        // register
        verify(gravitonApi, times(0)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        verify(gravitonApi, times(1)).get(anyString());
        // no /event/status update
        verify(gravitonApi, times(0)).patch(isA(EventStatus.class));
        
        // again, this time no request concern
        testWorker.handleRequestCalled = false;
        testWorker.isConcerningRequest = false;

        workerConsumer.consume("34343", message);
        
        assertTrue(testWorker.concerningRequestCalled);
        assertFalse(testWorker.handleRequestCalled);        
    }
    
    @Test
    public void testWorkerException() throws Exception {
        TestWorkerException testWorker = prepareTestWorker(new TestWorkerException());
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);

        // register
        verify(gravitonApi, times(1)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        // check if event status will be fetched before every update
        verify(gravitonApi, times(3)).get(anyString());
        // working update & failed update
        verify(gravitonApi, times(2)).patch(isA(EventStatus.class));
    }
    
    @Test
    public void testWorkerExceptionNoAuto() throws Exception {
        TestWorkerException testWorker = prepareTestWorker(new TestWorkerException());
        testWorker.doAutoStuff = false;
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);

        // register
        verify(gravitonApi, times(0)).put(isA(EventWorker.class));

        // 1 execution is due to the mock statement
        verify(gravitonApi, times(1)).get(anyString());
        // no /event/status update
        verify(gravitonApi, times(0)).patch(isA(EventStatus.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBackendStatusUpdateError() throws Exception {
        
        /*** change mocking so we get an unsuccessful response on /event/status update -> worker shall throw GravitonCommunicationException ***/
        when(gravitonApi.patch(any(EventStatus.class)).execute()).thenThrow(UnsuccessfulResponseException.class);
        
        TestWorker testWorker = prepareTestWorker(new TestWorker());
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw CommunicationException
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()), Charset.forName("UTF-8"));
        workerConsumer.consume("34343", message);
    }
    
    @Test
    public void testVcapConfiguration() throws Exception {
        
        TestWorker testWorker = prepareTestWorker(new TestWorker());
        worker = getWrappedWorker(testWorker);
        
        String vcapCreds = "{\"rabbitmq-3.0\": [{\"credentials\": {\"host\": \"test\", \"port\": 32321,"+
                "\"user\": \"hansuser\", \"password\": \"hans22\", \"virtualhost\": \"hanshost\"}}]}";
        
        when(worker.getVcap())
        .thenReturn(vcapCreds);        
        
        worker.run();

        assertEquals("test", worker.getProperties().getProperty("queue.host"));
        assertEquals("32321", worker.getProperties().getProperty("queue.port"));
        assertEquals("hansuser", worker.getProperties().getProperty("queue.user"));
        assertEquals("hans22", worker.getProperties().getProperty("queue.password"));
        assertEquals("hanshost", worker.getProperties().getProperty("queue.virtualhost"));
    }

    @Test
    public void testIsTerminatedState() throws Exception {
        TestWorker testWorker = prepareTestWorker(new TestWorker());

        assertTrue(testWorker.isTerminatedState(EventStatusStatus.Status.FAILED));
        assertTrue(testWorker.isTerminatedState(EventStatusStatus.Status.DONE));
        assertTrue(testWorker.isTerminatedState(EventStatusStatus.Status.IGNORED));
        assertFalse(testWorker.isTerminatedState(EventStatusStatus.Status.OPENED));
        assertFalse(testWorker.isTerminatedState(EventStatusStatus.Status.WORKING));
    }

    private <T extends WorkerAbstract> T prepareTestWorker(T worker) {
        worker.gravitonApi = gravitonApi;
        return worker;
    }

}
