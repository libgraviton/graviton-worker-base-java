package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.workerbase.WorkerBaseTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class})
public class WorkerUtilTest extends WorkerBaseTestCase {

    @Test
    public void testRqlEncoding() throws Exception {        
        assertEquals(
            "http%3A%2F%2Flocalhost%2Fdude%2Fhans%2Ffreddy%2D1%2C%2D1",
            WorkerUtil.encodeRql("http://localhost/dude/hans/freddy-1,-1")
        );
        assertEquals(
            "http%3A%2F%2Flocalhost%2Fdude%2Fhans%2Ffranz%2DX%5FX%2EX%7EX%2C%2Fpesche",
            WorkerUtil.encodeRql("http://localhost/dude/hans/franz-X_X.X~X,/pesche")
        );
    }

}
