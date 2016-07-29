package com.github.libgraviton.workerbase.model.status;

import com.github.libgraviton.workerbase.model.GravitonRef;

/**
 * <p>WorkerStatus class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class WorkerStatus {

    public String workerId;
    public Status status;
    public GravitonRef action;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public GravitonRef getAction() {
        return action;
    }

    public void setAction(GravitonRef action) {
        this.action = action;
    }
}
