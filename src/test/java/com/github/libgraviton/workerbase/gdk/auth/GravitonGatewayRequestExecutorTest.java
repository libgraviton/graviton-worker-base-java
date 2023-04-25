package com.github.libgraviton.workerbase.gdk.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.libgraviton.workerbase.gdk.GravitonGatewayRequestExecutor;
import com.github.libgraviton.workerbase.gdk.api.HttpMethod;
import com.github.libgraviton.workerbase.gdk.api.Request;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workertestbase.WorkerTestExtension;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class GravitonGatewayRequestExecutorTest {

    @BeforeEach
    public void setProps() {
        WorkerProperties.setOverride(WorkerProperties.GATEWAY_BASE_URL.toString(), workerTestExtension.getWiremockUrl());
    }

    @RegisterExtension
    public static WorkerTestExtension workerTestExtension = (new WorkerTestExtension())
            .setStartWiremock(true)
            .setStartRabbitMq(false);

    @Test
    public void testGatewayAuthHandling() throws MalformedURLException, CommunicationException, JsonProcessingException {
        GravitonGatewayRequestExecutor executor = DependencyInjection.getInstance(GravitonGatewayRequestExecutor.class);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode tokenNode = objectMapper.createObjectNode();
        tokenNode.put("token", "THIS-IS-THE-MAGIC-ACCESS-TOKEN");

        String tokenLifetime = WorkerProperties.GATEWAY_JWT_LIFETIME.get();

        final AtomicInteger callCounter = new AtomicInteger(0);

        // not successful reply!
        workerTestExtension.getWireMockServer().stubFor(
                requestMatching(value -> {
                    boolean normalCriteria = value.getUrl().equals("/auth") && value.getMethod().equals(RequestMethod.POST);
                    return MatchResult.of(normalCriteria && callCounter.incrementAndGet() < 5);
                })
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("x-jwt-lifetime", equalTo(tokenLifetime))
                        .willReturn(aResponse().withStatus(400))
        );

        // successful reply!
        workerTestExtension.getWireMockServer().stubFor(
                requestMatching(value -> {
                    boolean normalCriteria = value.getUrl().equals("/auth") && value.getMethod().equals(RequestMethod.POST);
                    return MatchResult.of(normalCriteria && callCounter.get() > 4);
                })
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("x-jwt-lifetime", equalTo(tokenLifetime))
                        .withRequestBody(equalTo("{\"username\":\"TESTUSERNAME\",\"password\":\"testpw\"}"))
                        .willReturn(aResponse().withBody(objectMapper.writeValueAsString(tokenNode)).withStatus(200))
        );

        workerTestExtension.getWireMockServer().stubFor(
                get(urlEqualTo("/fred/test"))
                        .withHeader("x-rest-token", equalTo("THIS-IS-THE-MAGIC-ACCESS-TOKEN"))
                        .willReturn(aResponse().withBody("YES").withStatus(200))
        );

        Request.Builder requestBuilder = new Request.Builder();
        Request req = requestBuilder.setUrl(WorkerProperties.GRAVITON_BASE_URL.get() + "/fred/test")
                .setMethod(HttpMethod.GET)
                .build();

        executor.execute(req);

        // close it!
        executor.close();

        // execute it again!
        executor.execute(req);

        executor.close();

        // 6 calls to /auth (5 retries not successful, last one ok)
        workerTestExtension.getWireMockServer().verify(6,
                postRequestedFor(urlEqualTo("/auth"))
        );
        // but 2 requests here..
        workerTestExtension.getWireMockServer().verify(2,
                getRequestedFor(urlEqualTo("/fred/test"))
        );
    }
}
