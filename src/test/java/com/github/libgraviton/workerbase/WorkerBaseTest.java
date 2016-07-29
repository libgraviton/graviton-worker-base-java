package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.lib.TestWorker;
import com.github.libgraviton.workerbase.lib.TestWorkerException;
import com.github.libgraviton.workerbase.lib.TestWorkerNoAuto;
import com.github.libgraviton.workerbase.model.GravitonRef;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workerbase.model.register.WorkerRegisterSubscription;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class,Unirest.class})
public class WorkerBaseTest extends WorkerBaseTestCase {

    @Before
    public void setUp() throws Exception {    
        this.baseMock();                    
    }

    @Test
    public void testRegistrationPreparation() throws Exception {
        TestWorker testWorker = new TestWorker();
        // to initialize worker
        worker = getWrappedWorker(testWorker);
        List<WorkerRegisterSubscription> subscriptions = testWorker.getSubscriptions();
        List<GravitonRef> actions = testWorker.getActions();
        assertEquals(1, subscriptions.size());
        assertEquals(2, actions.size());

        assertEquals("document.core.app.*", subscriptions.get(0).getEvent());
        assertEquals("http://localhost:8000/worker-base-default", actions.get(0).get$ref());
        assertEquals("http://localhost:8000/worker-base-default2", actions.get(1).get$ref());
    }

    @Test
    public void testBasicExecution() throws Exception {
        TestWorker testWorker = new TestWorker();
        worker = getWrappedWorker(testWorker);
        // execution should work even if message queue ack is not successful
        doThrow(new IOException()).when(queueChannel).basicAck(any(Long.class), any(Boolean.class));
        worker.run();
        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        verify(stringResponse, times(4)).getStatus();

        assertTrue(testWorker.shouldHandleRequestCalled);

        QueueEvent queueEvent = testWorker.getHandledQueueEvent();
        assertEquals("documents.core.app.create", queueEvent.getEvent());
        assertEquals("http://localhost/core/app/admin", queueEvent.getDocument().get$ref());
        assertEquals("http://localhost/event/status/mystatus", queueEvent.getStatus().get$ref());

        // register
        verify(requestBodyMock, times(1)).body(contains("\"id\":\"java-test\""));

        // working
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // done
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"done\",\"workerId\":\"java-test\""));

        // check if event status will be fetched before every update
        verify(statusResponse, times(2)).getBody();
    }

    @Test
    public void testStatusUpdateOnShouldNotHandleRequest() throws Exception {
        TestWorker testWorker = spy(new TestWorker());
        doReturn(false).when(testWorker).shouldHandleRequest(any(QueueEvent.class));
        worker = getWrappedWorker(testWorker);
        worker.run();

        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());

        verify(stringResponse, times(3)).getStatus();

        verify(testWorker, times(1)).shouldHandleRequest(any(QueueEvent.class));

        // register
        verify(requestBodyMock, times(1)).body(contains("\"id\":\"java-test\""));
        // working
        verify(requestBodyMock, never()).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // done
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"ignored\",\"workerId\":\"java-test\""));
    }
    
    @Test
    public void testNoAutoWorker() throws Exception {
        TestWorkerNoAuto testWorker = new TestWorkerNoAuto(); 
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        assertTrue(testWorker.concerningRequestCalled);
        assertTrue(testWorker.handleRequestCalled);
        
        verify(requestBodyMock, times(0)).body(anyString());
        
        // again, this time no request concern
        testWorker.handleRequestCalled = false;
        testWorker.isConcerningRequest = false;
        
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        assertTrue(testWorker.concerningRequestCalled);
        assertFalse(testWorker.handleRequestCalled);        
    }
    
    @Test
    public void testWorkerException() throws Exception {
        TestWorkerException testWorker = new TestWorkerException();        
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        // register
        verify(requestBodyMock, times(1)).body(contains("\"id\":\"java-test\""));
        // working update
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // failed update
        verify(requestBodyMock, times(1)).body(
                AdditionalMatchers.and(
                        contains("\"status\":\"failed\",\"workerId\":\"java-test\""),
                        contains("\"content\":\"com.github.libgraviton.workerbase.exception.WorkerException: Something bad happened!\"")
                        )
                );        
    }
    
    @Test
    public void testWorkerExceptionNoAuto() throws Exception {
        TestWorkerException testWorker = new TestWorkerException();
        testWorker.doAutoStuff = false;
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        // should be NO call on requestBodyMock
        verify(requestBodyMock, times(0)).body(anyString());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBackenStatusUpdateError() throws Exception {
        
        /*** change mocking so we get 400 status on worker registration -> worker shall throw GravitonCommunicationException ***/
        RequestBodyEntity bodyEntityRegister = mock(RequestBodyEntity.class);
        
        HttpResponse<String> stringResponseRegister = (HttpResponse<String>) mock(HttpResponse.class);
        when(stringResponseRegister.getStatus())
            .thenReturn(400);            
        when(bodyEntityRegister.asString())
        .thenReturn(stringResponseRegister); 

        HttpRequestWithBody registerBodyMock = mock(HttpRequestWithBody.class);
        
        when(registerBodyMock.routeParam(anyString(), anyString()))
            .thenReturn(registerBodyMock);
        when(registerBodyMock.header(anyString(), anyString()))
        .thenReturn(registerBodyMock);
        when(registerBodyMock.body(anyString()))
            .thenReturn(bodyEntityRegister);

        // PUT mock
        when(Unirest.put(contains("/event/status")))
            .thenReturn(registerBodyMock);    
        
        TestWorker testWorker = new TestWorker();
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw CommunicationException
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes()); 
    }
    
    @Test
    public void testVcapConfiguration() throws Exception {
        
        TestWorker testWorker = new TestWorker();
        worker = getWrappedWorker(testWorker);
        
        String vcapCreds = "{\"rabbitmq-3.0\": [{\"credentials\": {\"host\": \"test\", \"port\": 32321,"+
                "\"username\": \"hansuser\", \"password\": \"hans22\", \"vhost\": \"hanshost\"}}]}";
        
        when(worker.getVcap())
        .thenReturn(vcapCreds);        
        
        worker.run();

        assertEquals("test", worker.getProperties().getProperty("queue.host"));
        assertEquals("32321", worker.getProperties().getProperty("queue.port"));
        assertEquals("hansuser", worker.getProperties().getProperty("queue.username"));
        assertEquals("hans22", worker.getProperties().getProperty("queue.password"));
        assertEquals("hanshost", worker.getProperties().getProperty("queue.vhost"));        
    }

}
