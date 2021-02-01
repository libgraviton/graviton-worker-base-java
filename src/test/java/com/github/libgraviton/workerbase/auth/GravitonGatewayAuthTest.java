package com.github.libgraviton.workerbase.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.libgraviton.workerbase.gdk.api.Request.Builder;
import com.github.libgraviton.workerbase.auth.exception.CannotProcessAuth;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Test for Graviton Gateway Auth Strategy
 */
public class GravitonGatewayAuthTest {

    @Mock
    private Builder req;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private GravitonGatewayAuth auth;

    @Before
    public void setUp() throws Exception {
        auth = new GravitonGatewayAuth("http://localhost:8089/", "worker1", "hans");
        auth.setCoreUserId("testuser33");
    }

    @Test
    public void basicHandlingTest() throws Exception {
        // login
        stubFor(post(urlEqualTo("/auth"))
            .withRequestBody(equalToJson("{\"username\":\"worker1\",\"password\":\"hans\"}"))
            .withHeader("Content-Type", containing("application/json"))
            .withHeader("X-Impersonate", equalTo("testuser33"))
            .willReturn(aResponse()
            .withStatus(200)
            .withBodyFile("gravitonGatewayAuthResponse.json")));

        stubFor(options(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)));

        // logout
        stubFor(get(urlEqualTo("/security/logout"))
            .withHeader("X-REST-Token", equalTo("k_WAvqO4xhPOeB7dmVTvwYED712z7MKMAq9H0O2OMzI"))
            .willReturn(aResponse()
            .withStatus(200)
            .withBody("{}")));

        req = auth.beforeRequest(req);
/**
 // token injected into request?
 verify(req, times(1)).header("X-REST-Token", "k_WAvqO4xhPOeB7dmVTvwYED712z7MKMAq9H0O2OMzI");

 HttpResponse<JsonNode> mockResponse = (HttpResponse<JsonNode>) mock(HttpResponse.class);

 // logout being called on both other events?
 sut.onResponse(mockResponse);
 sut.onRequestFailure();

 List<LoggedRequest> requests = findAll(getRequestedFor(urlEqualTo("/security/logout")));
 assertEquals(2, requests.size());
 */
    }

    @Test
    public void failureReturnFromGatewayHandling() throws Exception {
        expectedEx.expect(CannotProcessAuth.class);
        expectedEx.expectMessage("Unable to locate token in authServiceUrl response.");

        stubFor(options(urlEqualTo("/"))
            .willReturn(aResponse()
                .withStatus(200)));

        stubFor(post(urlEqualTo("/auth"))
            .willReturn(aResponse()
            .withStatus(400)
            .withBody("{\"error\":\"User password is incorrect\",\"username\":\"worker1\"}")));

        req = auth.beforeRequest(req);
    }
}
