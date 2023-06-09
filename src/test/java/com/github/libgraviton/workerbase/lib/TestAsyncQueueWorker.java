package com.github.libgraviton.workerbase.lib;

import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.workerbase.AsyncQueueWorkerAbstract;
import com.github.libgraviton.workerbase.WorkerRunnableInterface;
import com.github.libgraviton.workerbase.annotation.GravitonWorker;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import io.activej.inject.annotation.Inject;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@GravitonWorker
public class TestAsyncQueueWorker extends AsyncQueueWorkerAbstract {

    public boolean onStartupCalled = false;

    public final AtomicInteger shouldHandleCallCount;
    public final AtomicInteger handleRequestCallCount;

    @Inject
    public TestAsyncQueueWorker(WorkerScope workerScope) {
        super(workerScope);
        shouldHandleCallCount = new AtomicInteger(0);
        handleRequestCallCount = new AtomicInteger(0);
    }

    public WorkerRunnableInterface handleRequestAsync(QueueEventScope queueEventScope) {
        return queueEventScope1 -> {
            try {
                LOG.info("START from thread {}", Thread.currentThread().getName());

                final CountDownLatch latch = new CountDownLatch(1);
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

                File file = queueEventScope1.getFileEndpoint().getFileFromQueueEvent(queueEventScope1.getQueueEvent());

                // correlate
                Assertions.assertEquals(
                  file.getId(),
                  queueEventScope1.getQueueEvent().getTransientHeaders().get("EVENT-NUMBER")
                );

                Assertions.assertEquals(
                  queueEventScope1.getQueueEvent().getCoreUserId(),
                  queueEventScope1.getScopeCacheMap().get("CHECK-THIS-VALUE")
                );

                int waitfor = RandomUtils.nextInt(500, 2000);

                executor.schedule(latch::countDown, waitfor, TimeUnit.MILLISECONDS);

                handleRequestCallCount.incrementAndGet();

                LOG.info("START AWAIT");
                latch.await();
                LOG.info("END AWAIT");
            } catch (Throwable t) {
                LOG.error("Error awaiting", t);
            }
        };
    }

    @Override
    public boolean shouldHandleRequest(QueueEventScope queueEventScope) {

        // set something im map!
        queueEventScope.getScopeCacheMap().put("CHECK-THIS-VALUE", queueEventScope.getQueueEvent().getCoreUserId());

        shouldHandleCallCount.incrementAndGet();
        return true;
    }

    @Override
    public void onStartUp() {
        onStartupCalled = true;
    }
}
