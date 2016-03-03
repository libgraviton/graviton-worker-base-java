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
    public GravitonRef eventResource;
    public List<WorkerStatus> status;
    public List<WorkerFeedback> information;
    
    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getId() {
        return id;
    }
    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id a {@link java.lang.String} object.
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * <p>Getter for the field <code>createDate</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCreateDate() {
        return createDate;
    }
    /**
     * <p>Setter for the field <code>createDate</code>.</p>
     *
     * @param createDate a {@link java.lang.String} object.
     */
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    /**
     * <p>Getter for the field <code>eventName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEventName() {
        return eventName;
    }
    /**
     * <p>Setter for the field <code>eventName</code>.</p>
     *
     * @param eventName a {@link java.lang.String} object.
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    /**
     * <p>Getter for the field <code>eventResource</code>.</p>
     *
     * @return a {@link GravitonRef} object.
     */
    public GravitonRef getEventResource() { return eventResource; }
    /**
     * <p>Setter for the field <code>eventResource</code>.</p>
     *
     * @param eventResource a {@link GravitonRef} object.
     */
    public void setEventResource(GravitonRef eventResource) { this.eventResource = eventResource; }
    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<WorkerStatus> getStatus() {
        return status;
    }
    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link java.util.List} object.
     */
    public void setStatus(List<WorkerStatus> status) {
        this.status = status;
    }
    /**
     * <p>Getter for the field <code>information</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<WorkerFeedback> getInformation() {
        return information;
    }
    /**
     * <p>Setter for the field <code>information</code>.</p>
     *
     * @param information a {@link java.util.List} object.
     */
    public void setInformation(List<WorkerFeedback> information) {
        this.information = information;
    }

    public void add(WorkerFeedback workerFeedback) {
        if (information == null) {
            information = new ArrayList<>();
        }
        getInformation().add(workerFeedback);
    }
}
