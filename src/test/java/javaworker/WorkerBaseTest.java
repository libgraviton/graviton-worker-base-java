package javaworker;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.libgraviton.workerbase.Worker;
import com.github.libgraviton.workerbase.WorkerAbstract;
import com.github.libgraviton.workerbase.WorkerConsumer;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Envelope;

import javaworker.lib.TestWorker;
import javaworker.lib.TestWorkerException;
import javaworker.lib.TestWorkerNoAuto;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class,Unirest.class})
public class WorkerBaseTest {
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();    
    
    private Worker worker;
    private WorkerConsumer workerConsumer;
    private TestWorker testWorker;
    private HttpResponse<JsonNode> jsonResponse;
    private Channel queueChannel;
    private ConnectionFactory connectionFactory;
    private RequestBodyEntity bodyEntity;
    private HttpRequestWithBody requestBodyMock;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        
        PowerMockito.mockStatic(Unirest.class);
        PowerMockito.mock(com.rabbitmq.client.ConnectionFactory.class);
        
        /**** UNIREST MOCKING ****/

        requestBodyMock = mock(HttpRequestWithBody.class);
        
        jsonResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);
        when(jsonResponse.getStatus())
            .thenReturn(204);
        
        bodyEntity = mock(RequestBodyEntity.class);
        when(bodyEntity.asJson())
            .thenReturn(jsonResponse);
        
        when(requestBodyMock.routeParam(anyString(), anyString()))
            .thenReturn(requestBodyMock);
        when(requestBodyMock.header(anyString(), anyString()))
        .thenReturn(requestBodyMock);
        when(requestBodyMock.body(anyString()))
            .thenReturn(bodyEntity);

        // PUT mock
        when(Unirest.put(anyString()))
            .thenReturn(requestBodyMock);
        
        // GET /event/status mock        
        URL statusResponseUrl = this.getClass().getClassLoader().getResource("json/statusResponse.json");
        String statusResponseContent = FileUtils.readFileToString(new File(statusResponseUrl.getFile()));
        GetRequest getRequestStatus = mock(GetRequest.class);
        HttpResponse<String> statusResponse = (HttpResponse<String>) mock(HttpResponse.class);
        when(statusResponse.getBody())
            .thenReturn(statusResponseContent);        
        when(getRequestStatus.header(anyString(), anyString()))
            .thenReturn(getRequestStatus);
        when(getRequestStatus.asString())
            .thenReturn(statusResponse);        
        when(Unirest.get(contains("/mystatus")))
            .thenReturn(getRequestStatus);
        
        /**** RABBITMQ MOCKING ****/
        
        Connection queueConnection = mock(Connection.class);
        queueChannel = mock(Channel.class);
        connectionFactory = mock(ConnectionFactory.class);
        DeclareOk queueDeclareOk = mock(DeclareOk.class);
        
        when(queueDeclareOk.getQueue())
            .thenReturn("graviton-masterqueue");
        
        when(queueChannel.queueDeclare())
            .thenReturn(queueDeclareOk);
        when(queueConnection.createChannel())
            .thenReturn(queueChannel);
        when(connectionFactory.newConnection())
            .thenReturn(queueConnection);                
    }
    
    private Worker getWrappedWorker(WorkerAbstract testWorker) {
        worker = spy(new Worker(testWorker));
        workerConsumer = PowerMockito.spy(new WorkerConsumer(queueChannel, testWorker));
        
        when(worker.getConnectionFactory())
            .thenReturn(connectionFactory);
        when(worker.getWorkerConsumer(any(Channel.class), any(WorkerAbstract.class)))
            .thenReturn(workerConsumer);
        
        return worker;
    }

    @Test
    public void testBasicExecution() throws Exception {
        testWorker = new TestWorker();
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        verify(jsonResponse, times(1)).getStatus();
        
        assertTrue(this.testWorker.concerningRequestCalled);
        
        assertTrue(this.outContent.toString().contains("Worker register response code: 204"));
        assertTrue(this.outContent.toString().contains("Subscribed on topic exchange 'graviton' using binding key 'document.core.app.*'"));
        assertTrue(this.outContent.toString().contains("the testworker has been executed!"));
        assertTrue(this.outContent.toString().contains("EVENT = documents.core.app.create"));
        assertTrue(this.outContent.toString().contains("DOCUMENT = http://localhost/core/app/admin"));
        assertTrue(this.outContent.toString().contains("STATUS = http://localhost/event/status/mystatus"));
        
        // register
        verify(requestBodyMock, times(1)).body(contains("{\"id\":\"java-test\""));
        // working
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // done
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"done\",\"workerId\":\"java-test\""));
    }
    
    @Test
    public void testNoAutoWorker() throws IOException {
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
    public void testWorkerException() throws IOException {
        TestWorkerException testWorker = new TestWorkerException();        
        worker = getWrappedWorker(testWorker);
        worker.run();
        
        // let worker throw WorkerException        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
        
        // register
        verify(requestBodyMock, times(1)).body(contains("{\"id\":\"java-test\""));
        // working update
        verify(requestBodyMock, times(1)).body(contains("\"status\":\"working\",\"workerId\":\"java-test\""));
        // failed update
        verify(requestBodyMock, times(1)).body(
                AdditionalMatchers.and(
                        contains("\"status\":\"failed\",\"workerId\":\"java-test\""),
                        contains("\"content\":\"com.github.libgraviton.workerbase.WorkerException: Something bad happened!\"")
                        )
                );        
    }
    
    @Test
    public void testWorkerExceptionNoAuto() throws IOException {
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
    
    @Test
    public void testVcapConfiguration() throws IOException {
        
        testWorker = new TestWorker();
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
