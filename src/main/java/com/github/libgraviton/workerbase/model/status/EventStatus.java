package com.github.libgraviton.workerbase.model.status;

import com.github.libgraviton.workerbase.model.GravitonRef;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>EventStatus class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class EventStatus {

    public String id;
    public String createDate;
    public String eventName;
    public String userId;
    public GravitonRef eventResource;
    public List<WorkerStatus> status;
    public List<WorkerFeedback> information;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public GravitonRef getEventResource() { return eventResource; }

    public void setEventResource(GravitonRef eventResource) { this.eventResource = eventResource; }

    public List<WorkerStatus> getStatus() {
        return status;
    }

    public void setStatus(List<WorkerStatus> status) {
        this.status = status;
    }

    public List<WorkerFeedback> getInformation() {
        return information;
    }

    public void setInformation(List<WorkerFeedback> information) {
        this.information = information;
    }

    public void add(WorkerFeedback workerFeedback) {
        if (information == null) {
            information = new ArrayList<>();
        }
        getInformation().add(workerFeedback);
    }

    public boolean hasStatusNeedForDescription(String workerId) {
        for (WorkerStatus statusEntry : status) {
            if (workerId.equals(statusEntry.getWorkerId())
                    && statusEntry.getDescription() != null
                    && !statusEntry.getDescription().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
