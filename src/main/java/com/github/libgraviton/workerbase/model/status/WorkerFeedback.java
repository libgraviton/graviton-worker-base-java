package com.github.libgraviton.workerbase.model.status;

/**
 * <p>WorkerFeedback class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class WorkerFeedback {

    public String workerId;
    public String content;
    public InformationType type;
    public String $ref;

    public WorkerFeedback(String workerId, InformationType informationType, String content) {
        this.workerId = workerId;
        this.type = informationType;
        this.content = content;
    }

    /**
     * <p>Getter for the field <code>workerId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getWorkerId() {
        return workerId;
    }
    /**
     * <p>Setter for the field <code>workerId</code>.</p>
     *
     * @param workerId a {@link java.lang.String} object.
     */
    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
    /**
     * <p>Getter for the field <code>content</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getContent() {
        return content;
    }
    /**
     * <p>Setter for the field <code>content</code>.</p>
     *
     * @param content a {@link java.lang.String} object.
     */
    public void setContent(String content) {
        this.content = content;
    }
    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return a {@link InformationType} object.
     */
    public InformationType getType() {
        return type;
    }
    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type a {@link InformationType} object.
     */
    public void setType(InformationType type) {
        this.type = type;
    }
    /**
     * <p>Getter for the field <code>$ref</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String get$ref() {
        return $ref;
    }
    /**
     * <p>Setter for the field <code>$ref</code>.</p>
     *
     * @param $ref a {@link java.lang.String} object.
     */
    public void set$ref(String $ref) {
        this.$ref = $ref;
    }
}
