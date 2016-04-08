package com.github.libgraviton.workerbase;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import com.github.libgraviton.workerbase.mq.QueueManager;
import com.github.libgraviton.workerbase.mq.WorkerQueueManager;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.powermock.api.mockito.PowerMockito;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;
import com.rabbitmq.client.Channel;

public abstract class WorkerBaseTestCase {
    
    protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    protected final ByteArrayOutputStream errContent = new ByteArrayOutputStream();    
    
    protected Worker worker;
    protected WorkerConsumer workerConsumer;
    
    protected HttpResponse<JsonNode> jsonResponse;
    protected HttpResponse<String> stringResponse;
    protected Channel queueChannel;
    protected RequestBodyEntity bodyEntity;
    protected HttpRequestWithBody requestBodyMock;
    protected HttpResponse<String> statusResponse;
    
    @SuppressWarnings("unchecked")
    @Before
    public void baseMock() throws Exception {
        
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        
        PowerMockito.mockStatic(Unirest.class);
        
        /**** UNIREST MOCKING ****/

        requestBodyMock = mock(HttpRequestWithBody.class);
        
        jsonResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);
        when(jsonResponse.getStatus())
            .thenReturn(204);        
        stringResponse = (HttpResponse<String>) mock(HttpResponse.class);
        when(stringResponse.getStatus())
            .thenReturn(204);    
        
        bodyEntity = mock(RequestBodyEntity.class);
        when(bodyEntity.asJson())
            .thenReturn(jsonResponse);
        when(bodyEntity.asString())
        .thenReturn(stringResponse); 
        
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
        statusResponse = (HttpResponse<String>) mock(HttpResponse.class);
        when(statusResponse.getBody())
            .thenReturn(statusResponseContent);        
        when(getRequestStatus.header(anyString(), anyString()))
            .thenReturn(getRequestStatus);
        when(getRequestStatus.asString())
            .thenReturn(statusResponse);        
        when(Unirest.get(contains("/mystatus")))
            .thenReturn(getRequestStatus);
    }
    
    protected Worker getWrappedWorker(WorkerAbstract testWorker) throws Exception {
        worker = spy(new Worker(testWorker));
        workerConsumer = PowerMockito.spy(new WorkerConsumer(queueChannel, testWorker));

        WorkerQueueManager queueManager = mock(WorkerQueueManager.class);
        when(worker.getQueueManager()).thenReturn(queueManager);
        doNothing().when(queueManager).connect();
        
        return worker;
    }

}
