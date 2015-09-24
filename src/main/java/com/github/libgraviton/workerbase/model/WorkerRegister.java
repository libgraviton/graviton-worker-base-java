package com.github.libgraviton.workerbase.model;

import java.util.ArrayList;

public class WorkerRegister {

    public String id;
    public ArrayList<WorkerRegisterSubscription> subscription;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public ArrayList<WorkerRegisterSubscription> getSubscription() {
        return subscription;
    }
    public void setSubscription(ArrayList<WorkerRegisterSubscription> subscription) {
        this.subscription = subscription;
    }
    
}