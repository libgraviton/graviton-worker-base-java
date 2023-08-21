package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventstatusaction.document.EventStatusAction;
import com.github.libgraviton.gdk.gravitondyn.eventworker.document.EventWorkerSubscription;
import com.github.libgraviton.workerbase.helper.RabbitMqMgmtClient;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import com.github.libgraviton.workerbase.util.CallbackRegistrar;
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
public abstract class QueueWorkerAbstract extends BaseWorker implements QueueWorkerInterface {

    private final String queueConnectionName;

    @Inject
    public QueueWorkerAbstract(WorkerScope workerScope) {
        super(workerScope);
        queueConnectionName = WorkerUtil.getQueueClientId();
    }

    public boolean shouldLinkAction(String workerId, List<EventStatusStatus> status) {
        for (EventStatusStatus statusEntry : status) {
            if (workerId.equals(statusEntry.getWorkerId())
                    && statusEntry.getAction() != null) {
                return false;
            }
        }
        return true;
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

    public void addCallbacks(CallbackRegistrar callbackRegistrar) {
    }

    /**
     * return here at which EventStatus state the queue message should be acknowledged
     * @return
     */
    public EventStatusStatus.Status getAcknowledgeState() {
        return EventStatusStatus.Status.DONE;
    }

    public List<EventWorkerSubscription> getSubscriptions() {
        String[] subscriptionKeys = WorkerProperties.GRAVITON_SUBSCRIPTION.get().split(",");
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
    public EventStatusStatusAction getWorkerAction() {
        String eventStatusActionEndpointUrl = workerScope.getGravitonApi()
                .getEndpointManager()
                .getEndpoint(EventStatusAction.class.getName())
                .getUrl();

        String workerId = getWorkerScope().getWorkerId();
        EventStatusStatusAction action = new EventStatusStatusAction();
        action.set$ref(String.format("%s%s-default", eventStatusActionEndpointUrl, workerId));
        return action;
    }

    public boolean doHealthCheck() {
        LOG.debug("Performing healthcheck for queue connection named '{}'", queueConnectionName);

        try {
            RabbitMqMgmtClient rabbitMqMgmtClient = new RabbitMqMgmtClient(
              WorkerProperties.QUEUE_HOST.get(),
              Integer.parseInt(WorkerProperties.QUEUE_MGMTPORT.get()),
              WorkerProperties.QUEUE_USER.get(),
              WorkerProperties.QUEUE_PASSWORD.get(),
              1
            );

            rabbitMqMgmtClient.ensureClientPresence(queueConnectionName);
            LOG.debug("Healthcheck succeeded.");

            return true;
        } catch (Throwable t) {
            LOG.warn("Healthcheck failed", t);
            return false;
        }
    }
}
