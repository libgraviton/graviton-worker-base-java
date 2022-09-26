package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.GravitonAuthApi;
import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventstatusaction.document.EventStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorker;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorkerSubscription;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.messaging.MessageAcknowledger;
import com.github.libgraviton.workerbase.messaging.exception.CannotAcknowledgeMessage;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.google.common.util.concurrent.AtomicLongMap;
import io.activej.inject.annotation.Inject;
import io.micrometer.core.instrument.*;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Abstract WorkerAbstract class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
@Inject
public abstract class QueueWorkerAbstract extends BaseWorker implements QueueWorkerInterface {

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

    protected EventStatusHandler statusHandler;

    protected String messageId;

    protected GravitonFileEndpoint fileEndpoint;

    protected MessageAcknowledger acknowledger;

    protected GravitonAuthApi gravitonApi;

    protected boolean areWeAsync = false;

    /**
     * which one of those states should we have until we acknowledge the queue event?
     */
    protected final List<EventStatusStatus.Status> acknowledgeStates = List.of(
            getAcknowledgeState(),
            EventStatusStatus.Status.FAILED,
            EventStatusStatus.Status.IGNORED
    );

    /**
     * these are all the "outcome" events, in which an QueueEvent can end in
     */
    protected final List<EventStatusStatus.Status> outcomeStates = List.of(
            EventStatusStatus.Status.DONE,
            EventStatusStatus.Status.FAILED,
            EventStatusStatus.Status.IGNORED
    );

    private ExecutorService executorService;
    
    /**
     * initializes this worker, will be called by the library
     *
     * @param properties properties
     * @throws WorkerException when a problem occurs that prevents the Worker from working properly
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     *
     */
    @Override
    public final void initialize(Properties properties) throws WorkerException, GravitonCommunicationException  {
        super.initialize(properties);

        gravitonApi = DependencyInjection.getInstance(GravitonAuthApi.class);
        statusHandler = DependencyInjection.getInstance(EventStatusHandler.class);
        fileEndpoint = DependencyInjection.getInstance(GravitonFileEndpoint.class);
        areWeAsync = (this instanceof AsyncQueueWorkerInterface);

        if (areWeAsync) {
            executorService = DependencyInjection.getInstance(ExecutorService.class);
        }

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

        if (shouldAutoRegister()) {
            register();
        }
    }

    /**
     * outer function that will be called on an queue event
     *
     * @param messageId delivery tag from envelope
     * @param acknowledger registered acknowledger
     * @param queueEvent queue event
     */
    public final void handleDelivery(QueueEvent queueEvent, String messageId, MessageAcknowledger acknowledger) {

        // mark as received
        eventReceivedCounter.increment();

        this.messageId = messageId;
        this.acknowledger = acknowledger;
        AtomicBoolean isAcknowledged = new AtomicBoolean(false);

        String statusUrl = convertToGravitonUrl(queueEvent.getStatus().get$ref());

        gravitonApi.setTransientHeaders(queueEvent.getTransientHeaders());

        // exception callback! -> will be used here and in the runnable!
        WorkerRunnable.AfterExceptionCallback exceptionCallback = (throwable) -> {
            // mark as errored
            eventStates.incrementAndGet(EventStatusStatus.Status.FAILED);

            // mark as not running anymore
            eventWorkingCounter.decrementAndGet();

            LOG.error("Error in worker {}: {}", workerId, throwable.getMessage(), throwable);

            if (shouldAutoUpdateStatus()) {
                try {
                    statusHandler.updateToErrorState(statusUrl, workerId, throwable.toString());
                } catch (GravitonCommunicationException e1) {
                    LOG.error("Unable to update worker status at '{}'.", statusUrl, e1);
                }
            }

            // failure acknowledge
            if (!isAcknowledged.get()) {
                // should we redeliver or not?
                if (shouldAutoAcknowledgeOnException()) {
                    // -> no redeliver!
                    acknowledgeToMessageQueue();
                    LOG.info("Acknowledged QueueEvent status '{}' as message ID '{}' to queue -> NO REDELIVER!", statusUrl, messageId);
                } else {
                    // -> DO redeliver!
                    acknowledgeFailToMessageQueue();
                    LOG.info("Acknowledged QueueEvent status '{}' as FAIL with message ID '{}' to queue -> should come back soon.", statusUrl, messageId);
                }
                isAcknowledged.set(true);
            }
        };

        // on status change this!
        WorkerRunnable.AfterStatusChangeCallback afterStatusChangeCallback = (status) -> {
            // if now working, mark as working
            if (status.equals(EventStatusStatus.Status.WORKING)) {
                eventWorkingCounter.incrementAndGet();
            }

            // is it done now?
            if (outcomeStates.contains(status)) {
                eventStates.incrementAndGet(status);
                // decrease working
                eventWorkingCounter.decrementAndGet();
            }

            // should we acknowledge now?
            if (acknowledgeStates.contains(status) && !isAcknowledged.get()) {
                acknowledgeToMessageQueue();
                LOG.info("Acknowledged QueueEvent status '{}' as message ID '{}' to queue.", statusUrl, messageId);
                isAcknowledged.set(true);
            }

            if (shouldAutoUpdateStatus()) {
                update(statusUrl, workerId, status);
            }
        };

        try {
            if (!shouldHandleRequest(queueEvent)) {
                eventStates.incrementAndGet(EventStatusStatus.Status.IGNORED);

                if (shouldAutoUpdateStatus()) {
                    update(statusUrl, workerId, EventStatusStatus.Status.IGNORED);
                }

                return;
            }

            WorkerRunnableInterface workload;
            if (areWeAsync) {
                workload = ((AsyncQueueWorkerInterface) this).handleRequestAsync(queueEvent);
            } else {
                workload = this::handleRequest;
            }

            // wrap with status handling stuff
            Runnable workerRunnable = new WorkerRunnable(
                    queueEvent,
                    workload,
                    afterStatusChangeCallback,
                    (workingDuration) -> {
                        LOG.info(
                                "QueueEvent processing is completed, working duration of '{}' ms. Message acknowledge state is '{}'.",
                                workingDuration.toMillis(),
                                isAcknowledged.get()
                        );

                        // record duration
                        eventWorkingDurationTimer.record(workingDuration);
                        // clear headers
                        gravitonApi.clearTransientHeaders();
                    },
                    exceptionCallback
            );

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

    /**
     * Rewrite the status URL in relation to the configured GravitonApi Base url.
     *
     * @param url url
     * @return corrected url
     */
    protected String convertToGravitonUrl(String url) {
        HttpUrl baseUrl = HttpUrl.parse(gravitonApi.getBaseUrl());

        // convert
        HttpUrl targetUrl = HttpUrl
            .parse(url)
            .newBuilder()
            .host(baseUrl.host())
            .port(baseUrl.port())
            .scheme(baseUrl.scheme())
            .build();

        return targetUrl.toString();
    }

    @Deprecated
    final protected void processDelivery(QueueEvent queueEvent, String statusUrl) throws WorkerException, GravitonCommunicationException {
        // DO NOT IMPLEMENT processDelivery anymore in the worker! marking as final, it's not called anymore..
    }

    protected void update(EventStatus eventStatus, String workerId, EventStatusStatus.Status status) throws GravitonCommunicationException {
        if (shouldLinkAction(workerId, eventStatus.getStatus())) {
            statusHandler.updateWithAction(eventStatus, workerId, status, getWorkerAction());
        } else {
            statusHandler.update(eventStatus, workerId, status);
        }
    }

    protected boolean shouldLinkAction(String workerId, List<EventStatusStatus> status) {
        for (EventStatusStatus statusEntry : status) {
            if (workerId.equals(statusEntry.getWorkerId())
                    && statusEntry.getAction() != null) {
                return false;
            }
        }
        return true;
    }

    protected void update(String eventStatusUrl, String workerId, EventStatusStatus.Status status) throws GravitonCommunicationException {
        update(statusHandler.getEventStatusFromUrl(eventStatusUrl), workerId, status);
    }

    private void acknowledgeToMessageQueue() {
        try {
            acknowledger.acknowledge(messageId);
        } catch (CannotAcknowledgeMessage e) {
            LOG.error("Unable to ack messageId '{}' on message queue.", messageId, e);
        }
    }

    private void acknowledgeFailToMessageQueue() {
        try {
            acknowledger.acknowledgeFail(messageId);
        } catch (CannotAcknowledgeMessage e) {
            LOG.error("Unable to nack messageId '{}' on message queue.", messageId, e);
        }
    }

    /**
     * can be overriden by worker implementation. should the lib automatically update the EventStatus in the backend?
     *
     * @return true if yes, false if not
     */
    public boolean shouldAutoUpdateStatus()
    {
        return true;
    }

    /**
     * can be overriden by worker implementation. should the lib acknowledge the queue action when an exception happened?
     *
     * @return true if yes, false if not
     */
    public boolean shouldAutoAcknowledgeOnException()
    {
        return false;
    }

    /**
     * can be overriden by worker implementation. should the lib automatically register the worker?
     *
     * @return true if yes, false if not
     */
    public boolean shouldAutoRegister()
    {
        return true;
    }

    /**
     * return here at which EventStatus state the queue message should be acknowledged
     * @return
     */
    public EventStatusStatus.Status getAcknowledgeState() {
        return EventStatusStatus.Status.DONE;
    }

    /**
     * registers our worker with the backend
     *
     * @throws GravitonCommunicationException when registering worker is not possible
     */
    protected void register() throws GravitonCommunicationException {
        EventWorker eventWorker = new EventWorker();
        eventWorker.setId(workerId);
        eventWorker.setSubscription(getSubscriptions());

        try {
            gravitonApi.put(eventWorker).execute();
        } catch (CommunicationException e) {
            throw new GravitonCommunicationException("Unable to register worker '" + workerId + "'.", e);
        }
    }

    protected List<EventWorkerSubscription> getSubscriptions() {
        String[] subscriptionKeys = properties.getProperty("graviton.subscription").split(",");
        List<EventWorkerSubscription> subscriptions = new ArrayList<>();
        for (String subscriptionKey: subscriptionKeys) {
            EventWorkerSubscription subscription = new EventWorkerSubscription();
            if (!subscriptionKey.isEmpty()) {
                subscription.setEvent(subscriptionKey.trim());
                subscriptions.add(subscription);
            }
        }
        return subscriptions;
    }

    /**
     * Links to the action the worker is processing. The action itself contains a localized description.
     *
     * @return link to worker action
     */
    protected EventStatusStatusAction getWorkerAction() {
        String eventStatusActionEndpointUrl = gravitonApi
                .getEndpointManager()
                .getEndpoint(EventStatusAction.class.getName())
                .getUrl();

        String workerId = properties.getProperty("graviton.workerId");
        EventStatusStatusAction action = new EventStatusStatusAction();
        action.set$ref(eventStatusActionEndpointUrl + workerId + "-default");
        return action;
    }

    public QueueManager getQueueManager() {
        return DependencyInjection.getInstance(QueueManager.class);
    }
}
