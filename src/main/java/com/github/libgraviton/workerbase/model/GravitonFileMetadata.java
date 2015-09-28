package com.github.libgraviton.workerbase.model;

import java.util.ArrayList;

public class GravitonFileMetadata {
    public Integer size;
    public String mime;
    public String createDate;
    public String modificationDate;
    public String filename;
    public ArrayList<GravitonFileMetadataAction> action;
    
    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    public String getMime() {
        return mime;
    }
    public void setMime(String mime) {
        this.mime = mime;
    }
    public String getCreateDate() {
        return createDate;
    }
    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
    public String getModificationDate() {
        return modificationDate;
    }
    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }
    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public ArrayList<GravitonFileMetadataAction> getAction() {
        return action;
    }
    public void setAction(ArrayList<GravitonFileMetadataAction> action) {
        this.action = action;
    }
    
}