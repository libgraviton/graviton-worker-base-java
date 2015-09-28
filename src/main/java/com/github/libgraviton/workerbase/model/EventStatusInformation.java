package com.github.libgraviton.workerbase.model;

public class EventStatusInformation {

    public String workerId;
    public String content;
    public String type;
    public String $ref;    
    
    public String getWorkerId() {
        return workerId;
    }
    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String get$ref() {
        return $ref;
    }
    public void set$ref(String $ref) {
        this.$ref = $ref;
    }
}