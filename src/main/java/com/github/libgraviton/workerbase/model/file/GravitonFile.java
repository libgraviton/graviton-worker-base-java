/**
 * An instance of this class represents a file of graviton's file service.
 */

package com.github.libgraviton.workerbase.model.file;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>GravitonFile class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
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
    public List<GravitonFileLinks> links;


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
    public List<GravitonFileLinks> getLinks() {
        return links;
    }

    /**
     * Returns all links of a given type.
     *
     * @param type The type to filter the links by.
     * @return A List holding all links matching the given type.
     * @since 0.7.0
     */
    public List<GravitonFileLinks> getLinks(String type) {
        List<GravitonFileLinks> links = new ArrayList<GravitonFileLinks>();
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
    public void setLinks(List<GravitonFileLinks> links) {
        this.links = links;
    }
}
