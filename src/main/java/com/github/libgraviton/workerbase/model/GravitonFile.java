package com.github.libgraviton.workerbase.model;

import java.util.ArrayList;

public class GravitonFile {
    public String id;
    public GravitonFileMetadata metadata;
    public ArrayList<GravitonFileLinks> links;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public GravitonFileMetadata getMetadata() {
        return metadata;
    }
    public void setMetadata(GravitonFileMetadata metadata) {
        this.metadata = metadata;
    }
    public ArrayList<GravitonFileLinks> getLinks() {
        return links;
    }
    public void setLinks(ArrayList<GravitonFileLinks> links) {
        this.links = links;
    }
}