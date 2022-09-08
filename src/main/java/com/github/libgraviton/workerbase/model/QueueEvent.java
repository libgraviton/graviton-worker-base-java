package com.github.libgraviton.workerbase.model;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>QueueEvent class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class QueueEvent {

    public static final String STATE_RECEIVED = "received";
    public static final String STATE_IGNORED = "ignored";
    public static final String STATE_HANDLED = "handled";
    public static final String STATE_ERRORED = "failed";

    public String event;
    public String coreUserId;
    public GravitonRef document;
    public GravitonRef status;
    public Map<String, String> transientHeaders = new HashMap<>();
    
    /**
     * <p>Getter for the field <code>event</code>.</p>
     *
     * @return string
     */
    public String getEvent() {
        return event;
    }
    /**
     * <p>Setter for the field <code>event</code>.</p>
     *
     * @param event a {@link java.lang.String} object.
     */
    public void setEvent(String event) {
        this.event = event;
    }
    /**
     * <p>Getter for the field <code>document</code>.</p>
     *
     * @return GravitonRef
     */
    public GravitonRef getDocument() {
        return document;
    }
    /**
     * <p>Setter for the field <code>document</code>.</p>
     *
     * @param document a {@link com.github.libgraviton.workerbase.model.GravitonRef} object.
     */
    public void setDocument(GravitonRef document) {
        this.document = document;
    }
    /**
     * <p>Getter for the field <code>status</code>.</p>
     *
     * @return GravitonRef
     */
    public GravitonRef getStatus() {
        return status;
    }
    /**
     * <p>Setter for the field <code>status</code>.</p>
     *
     * @param status a {@link com.github.libgraviton.workerbase.model.GravitonRef} object.
     */
    public void setStatus(GravitonRef status) {
        this.status = status;
    }
    /**
     * <p>Getter for the field <code>coreUserId</code>.</p>
     *
     * @return String
     */
    public String getCoreUserId() {
        return coreUserId;
    }
    /**
     * <p>Setter for the field <code>coreUserId</code>.</p>
     *
     * @param coreUserId a {@link java.lang.String} object.
     */
    public void setCoreUserId(String coreUserId) {
        this.coreUserId = coreUserId;
    }

    public Map<String, String> getTransientHeaders() {
        return transientHeaders;
    }

    public void setTransientHeaders(Map<String, String> transientHeaders) {
        this.transientHeaders = transientHeaders;
    }
}
