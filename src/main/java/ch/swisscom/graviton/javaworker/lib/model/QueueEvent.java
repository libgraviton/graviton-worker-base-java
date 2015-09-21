package ch.swisscom.graviton.javaworker.lib.model;

public class QueueEvent {
    public String event;
    public QueueEventRef document;
    public QueueEventRef status;
    
    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }
    public QueueEventRef getDocument() {
        return document;
    }
    public void setDocument(QueueEventRef document) {
        this.document = document;
    }
    public QueueEventRef getStatus() {
        return status;
    }
    public void setStatus(QueueEventRef status) {
        this.status = status;
    }   
    
    
}