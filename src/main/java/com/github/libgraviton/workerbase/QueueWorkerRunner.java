package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorker;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.NonExistingEventStatusException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.exception.CannotConnectToQueue;
import com.github.libgraviton.workerbase.messaging.exception.CannotRegisterConsumeable;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.google.common.util.concurrent.AtomicLongMap;
import io.activej.inject.annotation.Inject;
import io.micrometer.core.instrument.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class QueueWorkerRunner {

    private static final Logger LOG = LoggerFactory.getLogger(QueueWorkerRunner.class);

    @FunctionalInterface
    public interface AcknowledgeCallback {
        void onAck(boolean doNackAndRedeliver);
    }

    private final AtomicLongMap<EventStatusStatus.Status> eventStates = AtomicLongMap.create();
    private final AtomicLong eventWorkingCounter = new AtomicLong(0);
    private final Timer eventWorkingDurationTimer = Timer
            .builder("worker_queue_events_working_duration")
            .description("How long does it take to work on each queue event")
            .register(Metrics.globalRegistry);
    private final Counter eventReceivedCounter = Counter
            .builder("worker_queue_events_received")
            .description("Total queue events received")
            .register(Metrics.globalRegistry);

    /**
     * which one of those states should we have until we acknowledge the queue event?
     */
    protected final List<EventStatusStatus.Status> acknowledgeStates;

    /**
     * these are all the "outcome" events, in which an QueueEvent can end in
     */
    protected final List<EventStatusStatus.Status> outcomeStates = List.of(
            EventStatusStatus.Status.DONE,
            EventStatusStatus.Status.FAILED,
            EventStatusStatus.Status.IGNORED
    );

    private final QueueManager queueManager;

    private final boolean areWeAsync;
    private final QueueWorkerAbstract worker;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;

    private final List<WorkerRunnable.AfterCompleteCallback> afterCompleteCallbacks = new ArrayList<>();

    @Inject
    public QueueWorkerRunner(QueueWorkerAbstract worker) {
        this.queueManager = DependencyInjection.getInstance(QueueManager.class);
        areWeAsync = (this instanceof AsyncQueueWorkerInterface);
        if (areWeAsync) {
            executorService = DependencyInjection.getInstance(ExecutorService.class);
        } else {
            executorService = null;
        }

        this.worker = worker;
        this.objectMapper = DependencyInjection.getInstance(ObjectMapper.class);

        // register ack state
        acknowledgeStates = List.of(
            worker.getAcknowledgeState(),
            EventStatusStatus.Status.FAILED,
            EventStatusStatus.Status.IGNORED
        );

        // create our metrics -> outcome
        for (EventStatusStatus.Status theState : outcomeStates) {
            eventStates.put(theState, 0);

            Tags tags = Tags.of("state", theState.value());
            Gauge.builder("worker_queue_events_outcome", eventStates, map -> map.get(theState))
                    .tags(tags)
                    .description("Total worker queue events counts based on outcome.")
                    .register(Metrics.globalRegistry);
        }

        // gauge of how many are currently in working state
        Gauge.builder("worker_queue_events_working", eventWorkingCounter::get)
                .description("How many events are currently in working state.")
                .register(Metrics.globalRegistry);
    }

    public void addOnCompleteCallback(final WorkerRunnable.AfterCompleteCallback afterCompleteCallback) {
        afterCompleteCallbacks.add(afterCompleteCallback);
    }

    public void run() throws WorkerException {
        // should we register?
        if (worker.shouldAutoRegister()) {
            register();
        }

        try {
            queueManager.connect((messageId, message, acknowledger) -> {
                try {
                    final QueueEvent queueEvent = objectMapper.readValue(message, QueueEvent.class);
                    handleDelivery(queueEvent, messageId, acknowledger);
                } catch (Throwable t) {
                    LOG.error("Unable to work on message '{}'", message, t);
                }
            });
        } catch (CannotConnectToQueue | CannotRegisterConsumeable e) {
            throw new WorkerException("Unable to initialize worker.", e);
        }
    }

    public void close() {
        queueManager.close();
    }

    protected void register() throws WorkerException {
        EventWorker eventWorker = new EventWorker();
        eventWorker.setId(worker.getWorkerId());
        eventWorker.setSubscription(worker.getSubscriptions());

        try {
            worker.getWorkerScope().getGravitonApi().put(eventWorker).execute();
        } catch (CommunicationException e) {
            throw new WorkerException("Unable to register worker '" + worker.getWorkerId() + "'.", e);
        }
    }

    /**
     * outer function that will be called on an queue event
     *
     * @param messageId delivery tag from envelope
     * @param acknowledger registered acknowledger
     * @param queueEvent queue event
     */
    public final void handleDelivery(final QueueEvent queueEvent, final String messageId, final MessageAcknowledger acknowledger) {

        // mark as received
        eventReceivedCounter.increment();

        final AtomicBoolean isAcknowledged = new AtomicBoolean(false);
        final String statusUrl = worker.getWorkerScope().convertToGravitonUrl(queueEvent.getStatus().get$ref());

        final AcknowledgeCallback acknowledgeCallback = ((doNackAndRedeliver) -> {
            // ack or nack?
            if (doNackAndRedeliver) {
                // NACK - should be redelivered!
                try {
                    acknowledger.acknowledgeFail(messageId);
                    LOG.info("Acknowledged QueueEvent status '{}' as FAIL with message ID '{}' to queue -> should come back soon.", statusUrl, messageId);
                } catch (Throwable t) {
                    LOG.error("Unable to nack messageId '{}' on message queue.", messageId, t);
                }
            } else {
                // ACK -> no redeliver!
                try {
                    acknowledger.acknowledge(messageId);
                    LOG.info("Acknowledged QueueEvent status '{}' as message ID '{}' to queue -> NO REDELIVER!", statusUrl, messageId);
                } catch (Throwable t) {
                    LOG.error("Unable to ack messageId '{}' on message queue.", messageId, t);
                }
            }
        });

        // exception callback! -> will be used here and in the runnable!
        final WorkerRunnable.AfterExceptionCallback exceptionCallback = (throwable) -> {
            // mark as errored
            eventStates.incrementAndGet(EventStatusStatus.Status.FAILED);

            // logic should differ when event status does not exist!
            final boolean eventStatusDoesNotExist = (throwable instanceof NonExistingEventStatusException);

            LOG.error("Error in worker {}: {}", worker.getWorkerId(), throwable.getMessage(), throwable);

            if (worker.shouldAutoUpdateStatus()) {
                if (eventStatusDoesNotExist) {
                    LOG.warn("Will not try update status of EventStatus as it doesn't seem to exist ('{}')", throwable.getMessage());
                } else {
                    try {
                        worker.getWorkerScope().getStatusHandler().updateToErrorState(statusUrl, worker.getWorkerId(), throwable.toString());
                    } catch (GravitonCommunicationException e1) {
                        LOG.error("Unable to update worker status at '{}'.", statusUrl, e1);
                    }
                }
            }

            // failure acknowledge
            if (!isAcknowledged.get()) {
                // should we give up now?
                // NO if the worker wants redelivery
                boolean shouldWeGiveUp = worker.shouldAutoAcknowledgeOnException();
                // but YES if the eventstatus does not exist.
                if (!shouldWeGiveUp && eventStatusDoesNotExist) {
                    LOG.warn("Worker wants redelivery but we could not get the EventStatus because of 404 errors, so we need to give up! ('{}')", throwable.getMessage());
                    shouldWeGiveUp = true;
                }

                if (shouldWeGiveUp) {
                    // -> no redeliver!
                    acknowledgeCallback.onAck(false);
                } else {
                    acknowledgeCallback.onAck(true);
                }

                isAcknowledged.set(true);
            }
        };

        // on status change this!
        final WorkerRunnable.AfterStatusChangeCallback afterStatusChangeCallback = (status) -> {
            // if now working, mark as working
            if (status.equals(EventStatusStatus.Status.WORKING)) {
                eventWorkingCounter.incrementAndGet();
            }

            // is it done now?
            if (outcomeStates.contains(status)) {
                eventStates.incrementAndGet(status);
            }

            // should we acknowledge now?
            if (acknowledgeStates.contains(status) && !isAcknowledged.get()) {
                acknowledgeCallback.onAck(false);
                //LOG.info("Acknowledged QueueEvent status '{}' as message ID '{}' to queue.", statusUrl, messageId);
                isAcknowledged.set(true);
            }

            if (worker.shouldAutoUpdateStatus()) {
                worker.update(statusUrl, status);
            }
        };

        try {
            // wrap with status handling stuff
            final WorkerRunnable workerRunnable = new WorkerRunnable(
                queueEvent,
                (queueEventScope) -> {
                    final WorkerRunnableInterface workload;
                    if (areWeAsync) {
                        workload = ((AsyncQueueWorkerInterface) this).handleRequestAsync(queueEventScope);
                    } else {
                        workload = worker::handleRequest;
                    }
                    return workload;
                },
                afterStatusChangeCallback,
                (workingDuration) -> {

                    // decrement working
                    long workingCounter = eventWorkingCounter.decrementAndGet();
                    if (workingCounter < 0) {
                        eventWorkingCounter.set(0);
                    }

                    LOG.info(
                            "QueueEvent processing is completed, working duration of '{}' ms. Message acknowledge state is '{}'.",
                            workingDuration.toMillis(),
                            isAcknowledged.get()
                    );

                    // record duration
                    eventWorkingDurationTimer.record(workingDuration);
                },
                exceptionCallback,
                worker::shouldHandleRequest
            );

            afterCompleteCallbacks.forEach(workerRunnable::addAfterCompleteCallback);

            if (areWeAsync) {
                executorService.execute(workerRunnable);
            } else {
                // directly execute
                workerRunnable.run();
            }
        } catch (Throwable t) {
            exceptionCallback.onException(t);
        }
    }

}
