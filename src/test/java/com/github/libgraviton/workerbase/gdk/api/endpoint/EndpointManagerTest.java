package com.github.libgraviton.workerbase.gdk.api.endpoint;

import com.github.libgraviton.gdk.exception.NoCorrespondingEndpointException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EndpointManagerTest {

    private EndpointManager manager;

    private EndpointInclusionStrategy strategy;

    @Before
    public void setup() {
        manager = new EndpointManager();
        strategy = mock(EndpointInclusionStrategy.class);
        manager.setEndpointInclusionStrategy(strategy);
    }

    @Test
    public void testExistingEndpoint() throws Exception {
        String className = "aClassName";
        Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://collection/");
        manager.addEndpoint(className, endpoint);
        assertTrue(manager.hasEndpoint(className));
        assertEquals(endpoint, manager.getEndpoint(className));
    }

    @Test(expected = NoCorrespondingEndpointException.class)
    public void testMissingEndpoint() throws Exception {
        String className = "aClassName";
        assertFalse(manager.hasEndpoint(className));
        manager.getEndpoint(className);
    }

    @Test
    public void testShouldSkipEndpointWithStrategy() throws Exception {
        Endpoint endpoint = new Endpoint("endpoint://item", "endpoint://collection/");
        manager.shouldIgnoreEndpoint(endpoint);
        verify(strategy, times(1)).shouldIgnoreEndpoint(endpoint);
    }
}
