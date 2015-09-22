package javaworker;

import static org.junit.Assert.*;

import org.gravitonlib.workerbase.Worker;
import org.gravitonlib.workerbase.WorkerAbstract;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mashape.unirest.http.Unirest;

import javaworker.lib.TestWorker;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class,Unirest.class})
public class WorkerBaseTest {

    @Test
    public void test() {
        
        PowerMockito.mockStatic(Unirest.class);
        
        WorkerAbstract testWorker = new TestWorker();
        
        Worker worker = new Worker(testWorker);
        worker.run();        
        
    }

}
