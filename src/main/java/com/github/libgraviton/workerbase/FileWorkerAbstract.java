/**
 * abstract base class for file workers providing convenience.
 * it extends WorkerAbstract but provides more functions for /file api handling
 */

package com.github.libgraviton.workerbase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.model.GravitonFile;
import com.github.libgraviton.workerbase.model.GravitonFileMetadata;
import com.github.libgraviton.workerbase.model.GravitonFileMetadataAction;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract FileWorkerAbstract class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public abstract class FileWorkerAbstract extends WorkerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(FileWorkerAbstract.class);


    /**
     * gets file metadata from backend as a GravitonFile object
     *
     * @param url the url of the object
     * @throws java.lang.Exception if any.
     * @return file instance
     */
    public GravitonFile getGravitonFile(String url) throws Exception {
        HttpResponse<String> response = Unirest.get(url).header("Accept", "application/json").asString();
        return JSON.std.beanFrom(GravitonFile.class, response.getBody());
    }    
    
    /**
     * checks if a certain action is present in the metadata.action array
     *
     * @param gravitonFile a {@link com.github.libgraviton.workerbase.model.GravitonFile} object.
     * @param action a {@link java.lang.String} object.
     * @return true if yes, false if not
     */
    public Boolean isActionCommandPresent(GravitonFile gravitonFile, String action) {
        GravitonFileMetadata metadata = gravitonFile.getMetadata();
        for (GravitonFileMetadataAction singleAction: metadata.getAction()) {
            if (singleAction.getCommand() != null && singleAction.getCommand().equals(action) ) {
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
        GravitonFile gravitonFile = getGravitonFile(documentUrl);
        GravitonFileMetadata metadata = gravitonFile.getMetadata();
        List<GravitonFileMetadataAction> metadataActions = metadata.getAction();
        List<GravitonFileMetadataAction> matchingActions = new ArrayList<>();

        for (GravitonFileMetadataAction metadataAction : metadataActions) {
            if (action.equals(metadataAction.getCommand())) {
                matchingActions.add(metadataAction);
            }
        }

        if (matchingActions.size() > 0) {
            metadataActions.removeAll(matchingActions);

            Unirest.put(documentUrl).header("Content-Type", "application/json").body(JSON.std.asString(gravitonFile)).asString();
            LOG.info("[*] Removed action property from " + documentUrl);
        }        
    }
}
