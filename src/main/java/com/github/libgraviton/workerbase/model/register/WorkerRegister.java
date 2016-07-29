package com.github.libgraviton.workerbase.model.register;

import com.github.libgraviton.workerbase.model.GravitonRef;

import java.util.List;

/**
 * <p>WorkerRegister class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class WorkerRegister {

    public String id;
    public List<WorkerRegisterSubscription> subscription;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<WorkerRegisterSubscription> getSubscription() {
        return subscription;
    }

    public void setSubscription(List<WorkerRegisterSubscription> subscription) {
        this.subscription = subscription;
    }
}
