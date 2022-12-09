package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.Properties;

abstract class WorkerBaseTestCase {

    protected WorkerLauncher worker;
    protected WorkerConsumer workerConsumer;
    protected Channel queueChannel;
    protected GravitonApi gravitonApi;
    protected Response response;
    protected static ObjectMapper objectMapper;

    @BeforeEach
    void initAllBefore() {
        objectMapper = DependencyInjection.getInstance(ObjectMapper.class);
    }


    private void baseMock() {
        DependencyInjection.init(List.of());

        /**
        wiremock.stubFor(post(urlEqualTo("/event/worker"))
                .willReturn(
                        aResponse().withStatus(201)
                )
        );

        wiremock.stubFor(put(urlMatching("/event/worker/(.*)"))
                .willReturn(
                        aResponse().withStatus(201)
                )
        );

        wiremock.stubFor(put(urlMatching("/event/status/(.*)"))
                .willReturn(
                        aResponse().withBodyFile("eventStatusResponse.json").withStatus(200)
                )
        );

         **/

        /**

        response = mock(Response.class);
        gravitonApi = mock(DependencyInjection.getInstance(GravitonApi.class), RETURNS_DEEP_STUBS);

        // PUT mock
        when(gravitonApi.put(any(GravitonBase.class)).execute()).thenReturn(response);

        // PATCH mock
        when(gravitonApi.patch(any(GravitonBase.class)).execute()).thenReturn(response);
        
        // GET /event/status mock
        String body = FileUtils.readFileToString(
                new File("src/test/resources/json/statusResponse.json"), StandardCharsets.UTF_8);
        Response eventStatusResponse = spy(Response.class);
        eventStatusResponse.setObjectMapper(objectMapper);
        doReturn(body).when(eventStatusResponse).getBody();
        doCallRealMethod().when(eventStatusResponse).getBodyItem(eq(GravitonBase.class));
        when(gravitonApi.get(contains("/event/status")).execute()).thenReturn(eventStatusResponse);

        // url mock
        when(gravitonApi.getBaseUrl()).thenReturn("http://localhost:8080/");
         **/
    }
    
    protected WorkerLauncher getWrappedWorker(QueueWorkerAbstract testWorker) throws Exception {
        // set the class!
        WorkerProperties.setProperty("graviton.workerClass", testWorker.getClass().getName());
        WorkerInterface worker = DependencyInjection.getInstance(WorkerInterface.class);
        Properties properties = DependencyInjection.getInstance(Properties.class);

        /*
        worker = spy(new Worker(testWorker));
        queueChannel = mock(Channel.class);
        workerConsumer = spy(new WorkerConsumer(testWorker));
        workerConsumer.setAcknowledger(mock(MessageAcknowledger.class));

        QueueManager queueManager = mock(QueueManager.class);
        doNothing().when(queueManager).connect(any(QueueWorkerAbstract.class));

         */

        return new WorkerLauncher(
            worker,
            properties
        );
    }
}
