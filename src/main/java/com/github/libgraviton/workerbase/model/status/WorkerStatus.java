package com.github.libgraviton.workerbase.model.status;

/**
 * <p>WorkerStatus class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class WorkerStatus {

    public String workerId;
    public Status status;
    
    /**
     * <p>Getter for the field <code>workerId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWorkerId() {
        return workerId;
    }
    /**
     * <p>Setter for the field <code>workerId</code>.</p>
     *
     * @param workerId a {@link java.lang.String} object.
     */
    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link Status} object.
     */
    public Status getStatus() {
        return status;
    }
    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link Status} object.
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
}
