package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class WorkerTestRule implements TestRule {

    private static final Logger LOG = LoggerFactory.getLogger(WorkerTestRule.class);

    public WireMockServer wireMockServer;

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {

                List<Throwable> errors = new ArrayList<>();

                try {
                    WorkerProperties.load();
                    DependencyInjection.init(List.of());
                    startWiremock();
                    LOG.info("Running test {}...", description.getDisplayName());
                    statement.evaluate();
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    finished(description);
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };

    }

    protected void finished(Description description) {
        LOG.info("Entering test shutdown...");
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private void startWiremock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8080)); //No-args constructor will start on port 8080, no HTTPS
        wireMockServer.start();

        wireMockServer.stubFor(post(urlEqualTo("/event/worker"))
                .willReturn(
                        aResponse().withStatus(201)
                )
        );

        wireMockServer.stubFor(put(urlMatching("/event/worker/(.*)"))
                .willReturn(
                        aResponse().withStatus(201)
                )
        );

        wireMockServer.stubFor(put(urlMatching("/event/status/(.*)"))
                .willReturn(
                        aResponse().withBodyFile("eventStatusResponse.json").withStatus(200)
                )
        );
    }
}
