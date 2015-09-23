package javaworker;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.gravitonlib.workerbase.Worker;
import org.gravitonlib.workerbase.WorkerAbstract;
import org.gravitonlib.workerbase.WorkerConsumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class,Unirest.class})
public class WorkerBaseTest {

    @SuppressWarnings("unchecked")
    @Test
    public void test() throws Exception {
        
        PowerMockito.mockStatic(Unirest.class);
        PowerMockito.mock(com.rabbitmq.client.ConnectionFactory.class);
        
        /**** UNIREST MOCKING ****/

        HttpRequestWithBody requestBodyMock = Mockito.mock(HttpRequestWithBody.class);
        
        HttpResponse<JsonNode> jsonResponse = (HttpResponse<JsonNode>) Mockito.mock(HttpResponse.class);
        Mockito.when(jsonResponse.getStatus())
            .thenReturn(204);
        
        RequestBodyEntity bodyEntity = Mockito.mock(RequestBodyEntity.class);
        Mockito.when(bodyEntity.asJson())
            .thenReturn(jsonResponse);
        
        Mockito.when(requestBodyMock.routeParam(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.header(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.body(Mockito.anyString()))
            .thenReturn(bodyEntity);

        // PUT mock
        Mockito
            .when(Unirest.put(Mockito.anyString()))
            .thenReturn(requestBodyMock);
        
        // GET /event/status mock
        
        URL statusResponseUrl = this.getClass().getClassLoader().getResource("json/statusResponse.json");
        String statusResponseContent = FileUtils.readFileToString(new File(statusResponseUrl.getFile()));
        GetRequest getRequestStatus = Mockito.mock(GetRequest.class);
        HttpResponse<String> statusResponse = (HttpResponse<String>) Mockito.mock(HttpResponse.class);
        Mockito.when(statusResponse.getBody())
            .thenReturn(statusResponseContent);        
        Mockito
            .when(getRequestStatus.header(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(getRequestStatus);
        Mockito
            .when(getRequestStatus.asString())
            .thenReturn(statusResponse);        
        Mockito
            .when(Unirest.get(Mockito.contains("/mystatus")))
            .thenReturn(getRequestStatus);
        
        /**** RABBITMQ MOCKING ****/
        
        Connection queueConnection = Mockito.mock(Connection.class);
        Channel queueChannel = Mockito.mock(Channel.class);
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        DeclareOk queueDeclareOk = Mockito.mock(DeclareOk.class);
        Mockito.when(queueDeclareOk.getQueue())
            .thenReturn("graviton-masterqueue");
        
        Mockito.when(queueChannel.queueDeclare())
            .thenReturn(queueDeclareOk);
        Mockito.when(queueConnection.createChannel())
            .thenReturn(queueChannel);
        Mockito.when(connectionFactory.newConnection())
            .thenReturn(queueConnection);
        
        WorkerAbstract testWorker = new TestWorker();
        
        Worker worker = Mockito.spy(new Worker(testWorker));
        //new WorkerConsumer(queueChannel, worker)
        WorkerConsumer workerConsumer = PowerMockito.spy(new WorkerConsumer(queueChannel, testWorker));
        Mockito.when(worker.getConnectionFactory())
            .thenReturn(connectionFactory);
        Mockito.when(worker.getWorkerConsumer(Mockito.any(Channel.class), Mockito.any(WorkerAbstract.class)))
            .thenReturn(workerConsumer);
        
        worker.run();
        
        Envelope envelope = new Envelope(new Long(34343), false, "graviton", "documents.core.app.update");
        URL jsonFile = this.getClass().getClassLoader().getResource("json/queueEvent.json");
        String message = FileUtils.readFileToString(new File(jsonFile.getFile()));
        
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
    }

}
