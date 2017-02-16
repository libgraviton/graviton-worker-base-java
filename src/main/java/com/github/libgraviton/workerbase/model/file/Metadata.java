package com.github.libgraviton.workerbase.model.file;

import java.util.List;

/**
 * <p>Metadata class.</p>
 *
 * @author Dario Nuevo
 * @version $Id: $Id
 */
public class Metadata {
    public Integer size;
    public String mime;
    public String createDate;
    public String modificationDate;
    public String filename;
    public String hash;
    public String additionalInformation;
    public List<MetadataAction> action;
    public List<MetadataAddProperty> additionalProperties;
    
    /**
     * <p>Getter for the field <code>size</code>.</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getSize() {
        return size;
    }
    /**
     * <p>Setter for the field <code>size</code>.</p>
     *
     * @param size a {@link java.lang.Integer} object.
     */
    public void setSize(Integer size) {
        this.size = size;
    }
    /**
     * <p>Getter for the field <code>mime</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMime() {
        return mime;
    }
    /**
     * <p>Setter for the field <code>mime</code>.</p>
     *
     * @param mime a {@link java.lang.String} object.
     */
    public void setMime(String mime) {
        this.mime = mime;
    }
    /**
     * <p>Getter for the field <code>createDate</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCreateDate() {
        return createDate;
    }
    /**
     * <p>Setter for the field <code>createDate</code>.</p>
     *
     * @param createDate a {@link java.lang.String} object.
     */
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    /**
     * <p>Getter for the field <code>modificationDate</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getModificationDate() {
        return modificationDate;
    }
    /**
     * <p>Setter for the field <code>modificationDate</code>.</p>
     *
     * @param modificationDate a {@link java.lang.String} object.
     */
    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }
    /**
     * <p>Getter for the field <code>filename</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFilename() {
        return filename;
    }
    /**
     * <p>Setter for the field <code>filename</code>.</p>
     *
     * @param filename a {@link java.lang.String} object.
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    /**
     * <p>Getter for the field <code>hash</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHash() {
        return hash;
    }
    /**
     * <p>Setter for the field <code>hash</code>.</p>
     *
     * @param hash a {@link java.lang.String} object.
     */
    public void setHash(String hash) {
        this.hash = hash;
    }
    /**
     * <p>Getter for the field <code>action</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<MetadataAction> getAction() {
        return action;
    }
    /**
     * <p>Setter for the field <code>action</code>.</p>
     *
     * @param action a {@link java.util.List} object.
     */
    public void setAction(List<MetadataAction> action) {
        this.action = action;
    }
    /**
     * <p>Getter for the field <code>additionalInformation</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     * @since 0.7.0
     */
    public String getAdditionalInformation() {
        return this.additionalInformation;
    }
    
    /**
     * <p>Setter for the field <code>additionalInformation</code>.</p>
     *
     * @param additionalInformation a {@link java.lang.String} object.
     * @since 0.7.0
     */
    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    /**
     * <p>Getter for the field <code>additionalProperties</code>.</p>
     *
     * @return a {@link java.util.List} object.
     * @since 0.7.0
     */
    public List<MetadataAddProperty> getAdditionalProperties() {
        return this.additionalProperties;
    }

    /**
     * <p>Setter for the field <code>additionalProperties</code>.</p>
     *
     * @param additionalProperties a {@link java.util.List} object.
     * @since 0.7.0
     */
    public void setAdditionalProperties(List<MetadataAddProperty> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
    
}