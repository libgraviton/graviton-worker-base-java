package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.workerbase.gdk.exception.NoCorrespondingEndpointException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class EndpointManagerTest {

    private EndpointManager manager;

    private EndpointInclusionStrategy strategy;

    @BeforeEach
    public void setup() {
        manager = new EndpointManager();
        strategy = mock(EndpointInclusionStrategy.class);
        manager.setEndpointInclusionStrategy(strategy);
    }

    @Test
    public void testExistingEndpoint() {
        String className = "aClassName";
        Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://collection/");
        manager.addEndpoint(className, endpoint);
        Assertions.assertTrue(manager.hasEndpoint(className));
        Assertions.assertEquals(endpoint, manager.getEndpoint(className));
    }

    @Test
    public void testMissingEndpoint() {
        Assertions.assertThrows(NoCorrespondingEndpointException.class, () -> {
            String className = "aClassName";
            Assertions.assertFalse(manager.hasEndpoint(className));
            manager.getEndpoint(className);
        });
    }

    @Test
    public void testShouldSkipEndpointWithStrategy() {
        Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://collection/");
        manager.shouldIgnoreEndpoint(endpoint);
        verify(strategy, times(1)).shouldIgnoreEndpoint(endpoint);
    }
}
