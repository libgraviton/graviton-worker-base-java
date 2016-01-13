package com.github.libgraviton.workerbase.model;

/**
 * <p>QueueEvent class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class QueueEvent {
    public String event;
    public GravitonRef document;
    public GravitonRef status;
    
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
    
    
}
