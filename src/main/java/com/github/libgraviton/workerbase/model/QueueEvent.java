package com.github.libgraviton.workerbase.model;

public class QueueEvent {
    public String event;
    public GravitonRef document;
    public GravitonRef status;
    
    /**
     * 
     * @return string
     */
    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }
    /**
     * 
     * @return GravitonRef
     */
    public GravitonRef getDocument() {
        return document;
    }
    public void setDocument(GravitonRef document) {
        this.document = document;
    }
    /**
     * 
     * @return GravitonRef
     */
    public GravitonRef getStatus() {
        return status;
    }
    public void setStatus(GravitonRef status) {
        this.status = status;
    }   
    
    
}