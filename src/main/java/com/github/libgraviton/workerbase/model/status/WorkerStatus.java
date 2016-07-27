package com.github.libgraviton.workerbase.model.status;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>WorkerStatus class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class WorkerStatus {

    public String workerId;
    public Status status;
    public Map<String, String> description;

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public String getDescription(String language) {
        return description.get(language);
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public void addTranslatedDescription(String language, String content) {
        if(description == null) {
            description = new HashMap<>();
        }

        description.put(language, content);
    }
    
}
