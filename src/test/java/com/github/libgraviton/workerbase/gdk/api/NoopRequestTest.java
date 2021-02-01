package com.github.libgraviton.workerbase.gdk.api;

import com.github.libgraviton.gdk.exception.CommunicationException;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NoopRequestTest {

    @Test
    public void testExecute() throws CommunicationException {
        Response response = new NoopRequest.Builder("no reason")
                .setUrl("http://url-to-a-beautiful-place")
                .execute();
        assertNotNull(response);
        assertTrue(response instanceof NoopResponse);
    }
}
