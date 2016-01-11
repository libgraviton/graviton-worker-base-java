/**
 * abstract base class for file workers providing convenience.
 * it extends WorkerAbstract but provides more functions for /file api handling
 */

package com.github.libgraviton.workerbase;

import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.model.GravitonFile;
import com.github.libgraviton.workerbase.model.GravitonFileMetadata;
import com.github.libgraviton.workerbase.model.GravitonFileMetadataAction;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

/**
 * <p>Abstract FileWorkerAbstract class.</p>
 *
 * @author List of contributors
 *         <https://github.com/libgraviton/graviton/graphs/contributors>
 * @link http://swisscom.ch
 * @version $Id: $Id
 */
public abstract class FileWorkerAbstract extends WorkerAbstract {

    /**
     * gets file metadata from backend as a GravitonFile object
     *
     * @param url the url of the object
     * @throws java.lang.Exception if any.
     * @return file instance
     */
    public GravitonFile getGravitonFile(String url) throws Exception {
        HttpResponse<String> fileObj = Unirest.get(url).header("Accept", "application/json").asString();
        return JSON.std.beanFrom(GravitonFile.class, fileObj.getBody());        
    }    
    
    /**
     * checks if a certain action is present in the metadata.action array
     *
     * @param fileObj a {@link com.github.libgraviton.workerbase.model.GravitonFile} object.
     * @param action a {@link java.lang.String} object.
     * @return true if yes, false if not
     */
    public Boolean isActionCommandPresent(GravitonFile fileObj, String action) {
        GravitonFileMetadata metadata = fileObj.getMetadata();
        for (GravitonFileMetadataAction singleAction: metadata.getAction()) {
            if (singleAction.getCommand() != null &&
                    singleAction.getCommand().equals(action)
                    ) {
                return true;
            }            
        }
        
        return false;        
    }
    
    /**
     * removes the action.0.command action from the /file resource, PUTs it to the backend removed
     *
     * @param documentUrl document url
     * @param action action string to remove
     * @throws java.lang.Exception if any.
     */
    public void removeFileActionCommand(String documentUrl, String action) throws Exception {
        // we will re-fetch again just to be sure.. this *really* should use PATCH ;-)
        GravitonFile fileObj = this.getGravitonFile(documentUrl);
        GravitonFileMetadata metadata = fileObj.getMetadata();
        ArrayList<GravitonFileMetadataAction> metadataAction = metadata.getAction();
        
        boolean hasBeenRemoved = false;
        Iterator<GravitonFileMetadataAction> iter = metadataAction.iterator();
        
        while (iter.hasNext()) {
            GravitonFileMetadataAction singleAction = iter.next();
            if (singleAction.getCommand() != null &&
                    singleAction.getCommand().equals(action)
                    ) {
                iter.remove();
                hasBeenRemoved = true;
            }
        }
        
        if (hasBeenRemoved) {
            Unirest.put(documentUrl).header("Content-Type", "application/json").body(JSON.std.asString(fileObj)).asString();
            System.out.println(" [*] Removed action property from " + documentUrl);
        }        
    }
}
