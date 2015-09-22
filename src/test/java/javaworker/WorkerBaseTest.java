package javaworker;

import static org.junit.Assert.*;

import java.io.IOException;

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
import com.mashape.unirest.http.exceptions.UnirestException;
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

        Mockito
            .when(Unirest.put(Mockito.anyString()))
            .thenReturn(requestBodyMock);
        
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
        String message = "duuud";
        workerConsumer.handleDelivery("documents.core.app.update", envelope, new AMQP.BasicProperties(), message.getBytes());
    }

}
