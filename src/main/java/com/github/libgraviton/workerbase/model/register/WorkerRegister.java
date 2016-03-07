package com.github.libgraviton.workerbase.model.register;

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
     * <p>Getter for the field <code>subscription</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<WorkerRegisterSubscription> getSubscription() {
        return subscription;
    }
    /**
     * <p>Setter for the field <code>subscription</code>.</p>
     *
     * @param subscription a {@link java.util.List} object.
     */
    public void setSubscription(List<WorkerRegisterSubscription> subscription) {
        this.subscription = subscription;
    }
    
}
