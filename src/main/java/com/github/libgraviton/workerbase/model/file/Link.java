package com.github.libgraviton.workerbase.model.file;

/**
 * <p>Link class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class Link {
    public String type;
    public String $ref;
    
    /**
     * <p>Getter for the field <code>type</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return type;
    }
    /**
     * <p>Setter for the field <code>type</code>.</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
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
