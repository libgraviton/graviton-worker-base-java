package com.github.libgraviton.workerbase.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkerUtilTest {

    @Test
    public void testRqlEncoding() {
        Assertions.assertEquals(
            "http%3A%2F%2Flocalhost%2Fdude%2Fhans%2Ffreddy%2D1%2C%2D1",
            WorkerUtil.encodeRql("http://localhost/dude/hans/freddy-1,-1")
        );
        Assertions.assertEquals(
            "http%3A%2F%2Flocalhost%2Fdude%2Fhans%2Ffranz%2DX%5FX%2EX%7EX%2C%2Fpesche",
            WorkerUtil.encodeRql("http://localhost/dude/hans/franz-X_X.X~X,/pesche")
        );
    }

}
