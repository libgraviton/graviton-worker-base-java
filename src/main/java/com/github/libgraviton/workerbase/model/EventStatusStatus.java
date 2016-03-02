package com.github.libgraviton.workerbase.model;

/**
 * <p>EventStatusStatus class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class EventStatusStatus {

    public String workerId;
    public WorkerStatus status;
    
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
     * @return a {@link WorkerStatus} object.
     */
    public WorkerStatus getStatus() {
        return status;
    }
    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link WorkerStatus} object.
     */
    public void setStatus(WorkerStatus status) {
        this.status = status;
    }
    
}
