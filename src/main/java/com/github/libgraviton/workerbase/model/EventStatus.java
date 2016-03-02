package com.github.libgraviton.workerbase.model;

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
    public List<EventStatusStatus> status;
    public List<EventStatusInformation> information;
    
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
    public List<EventStatusStatus> getStatus() {
        return status;
    }
    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link java.util.List} object.
     */
    public void setStatus(List<EventStatusStatus> status) {
        this.status = status;
    }
    /**
     * <p>Getter for the field <code>information</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<EventStatusInformation> getInformation() {
        return information;
    }
    /**
     * <p>Setter for the field <code>information</code>.</p>
     *
     * @param information a {@link java.util.List} object.
     */
    public void setInformation(List<EventStatusInformation> information) {
        this.information = information;
    }    
}
