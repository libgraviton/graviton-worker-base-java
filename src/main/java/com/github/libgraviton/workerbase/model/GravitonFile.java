/**
 * An instance of this class represents a file of graviton's file service.
 */

package com.github.libgraviton.workerbase.model;

import java.util.ArrayList;

/**
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 */
public class GravitonFile {

    /**
     * id
     */
    public String id;

    /**
     * metadata
     */
    public GravitonFileMetadata metadata;

    /**
     * links
     */
    public ArrayList<GravitonFileLinks> links;


    /**
     * Get id
     * 
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Set id
     * 
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get metadata
     * 
     * @return The metadata
     */
    public GravitonFileMetadata getMetadata() {
        return metadata;
    }

    /**
     * Set metadata
     * 
     * @param metadata The metadata to set
     */
    public void setMetadata(GravitonFileMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Get links
     *  
     * @return The links
     */
    public ArrayList<GravitonFileLinks> getLinks() {
        return links;
    }

    /**
     * Returns all links of a given type.
     * 
     * @param type The type to filter the links by.
     * @return An ArrayList holding all links matching the given type.
     */
    public ArrayList<GravitonFileLinks> getLinks(String type) {
        ArrayList<GravitonFileLinks> links = new ArrayList<GravitonFileLinks>();
        for(GravitonFileLinks link : this.getLinks()) {
            if(link.getType().equals(type)) {
                links.add(link);
            }
        }
        return links;
    }

    /**
     * Set links
     * 
     * @param links The links to set
     */
    public void setLinks(ArrayList<GravitonFileLinks> links) {
        this.links = links;
    }
}