package com.github.libgraviton.workerbase.model;

import java.util.ArrayList;

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
    public ArrayList<EventStatusStatus> status;
    public ArrayList<EventStatusInformation> information;
    
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
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<EventStatusStatus> getStatus() {
        return status;
    }
    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link java.util.ArrayList} object.
     */
    public void setStatus(ArrayList<EventStatusStatus> status) {
        this.status = status;
    }
    /**
     * <p>Getter for the field <code>information</code>.</p>
     *
     * @return a {@link java.util.ArrayList} object.
     */
    public ArrayList<EventStatusInformation> getInformation() {
        return information;
    }
    /**
     * <p>Setter for the field <code>information</code>.</p>
     *
     * @param information a {@link java.util.ArrayList} object.
     */
    public void setInformation(ArrayList<EventStatusInformation> information) {
        this.information = information;
    }    
}
