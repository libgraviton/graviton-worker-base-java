package com.github.libgraviton.workerbase.model;

import java.util.ArrayList;

public class EventStatus {

    public String id;
    public String createDate;
    public String eventName;
    public ArrayList<EventStatusStatus> status;
    public ArrayList<EventStatusErrorInformation> errorInformation;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getCreateDate() {
        return createDate;
    }
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public ArrayList<EventStatusStatus> getStatus() {
        return status;
    }
    public void setStatus(ArrayList<EventStatusStatus> status) {
        this.status = status;
    }
    public ArrayList<EventStatusErrorInformation> getErrorInformation() {
        return errorInformation;
    }
    public void setErrorInformation(ArrayList<EventStatusErrorInformation> errorInformation) {
        this.errorInformation = errorInformation;
    }
    
}