package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.annotation.GravitonWorkerDiScan;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventstatusaction.document.EventStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorkerSubscription;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import io.activej.inject.annotation.Inject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Abstract WorkerAbstract class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
@GravitonWorkerDiScan
public abstract class QueueWorkerAbstract extends BaseWorker implements QueueWorkerInterface {

    @Inject
    public QueueWorkerAbstract(WorkerScope workerScope) {
        super(workerScope);
    }

    protected void update(EventStatus eventStatus, String workerId, EventStatusStatus.Status status) throws GravitonCommunicationException {
        if (shouldLinkAction(workerId, eventStatus.getStatus())) {
            workerScope.getStatusHandler().updateWithAction(eventStatus, workerId, status, getWorkerAction());
        } else {
            workerScope.getStatusHandler().update(eventStatus, workerId, status);
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

    protected void update(String eventStatusUrl, EventStatusStatus.Status status) throws GravitonCommunicationException {
        update(workerScope.getStatusHandler().getEventStatusFromUrl(eventStatusUrl), getWorkerId(), status);
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

    protected List<EventWorkerSubscription> getSubscriptions() {
        String[] subscriptionKeys = WorkerProperties.getProperty(WorkerProperties.GRAVITON_SUBSCRIPTION).split(",");
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
        String eventStatusActionEndpointUrl = workerScope.getGravitonApi()
                .getEndpointManager()
                .getEndpoint(EventStatusAction.class.getName())
                .getUrl();

        String workerId = getWorkerScope().getWorkerId();
        EventStatusStatusAction action = new EventStatusStatusAction();
        action.set$ref(eventStatusActionEndpointUrl + workerId + "-default");
        return action;
    }
}
