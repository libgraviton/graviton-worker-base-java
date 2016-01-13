package javaworker;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.libgraviton.workerbase.WorkerUtil;
import com.mashape.unirest.http.Unirest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class,Unirest.class})
public class WorkerUtilTest extends WorkerBaseTestCase {


    @Test
    public void testRqlEncoding() throws Exception {        
        String url = "http://localhost/dude/hans/freddy-1,1";        
        assertEquals("http%3A%2F%2Flocalhost%2Fdude%2Fhans%2Ffreddy-1%2C1", WorkerUtil.encodeRql(url));
    }
    

}
