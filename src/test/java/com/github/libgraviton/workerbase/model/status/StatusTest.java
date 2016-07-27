package com.github.libgraviton.workerbase.model.status;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StatusTest {

    @Test
    public void testIsTerminatedState() throws Exception {
        assertTrue(Status.FAILED.isTerminatedState());
        assertTrue(Status.DONE.isTerminatedState());
        assertTrue(Status.IGNORED.isTerminatedState());
        assertFalse(Status.OPEN.isTerminatedState());
        assertFalse(Status.WORKING.isTerminatedState());
    }

}
