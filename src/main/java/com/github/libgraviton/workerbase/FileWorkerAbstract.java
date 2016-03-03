/**
 * abstract base class for file workers providing convenience.
 * it extends WorkerAbstract but provides more functions for /file api handling
 */

package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.model.file.GravitonFile;
import com.github.libgraviton.workerbase.model.file.GravitonFileMetadata;
import com.github.libgraviton.workerbase.model.file.GravitonFileMetadataAction;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * @param fileUrl the url of the object
     * @throws GravitonCommunicationException if file could not be fetched.
     * @return file instance
     */
    public GravitonFile getGravitonFile(String fileUrl) throws GravitonCommunicationException {
        try {
            HttpResponse<String> response = Unirest.get(fileUrl).header("Accept", "application/json").asString();
            return JSON.std.beanFrom(GravitonFile.class, response.getBody());
        } catch (UnirestException | IOException e) {
            throw new GravitonCommunicationException("Unable to GET graviton file from '" + fileUrl + "'.", e);
        }

    }    
    
    /**
     * checks if a certain action is present in the metadata.action array
     *
     * @param gravitonFile a {@link GravitonFile} object.
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
     * @throws GravitonCommunicationException when file action command could not be removed at Graviton
     */
    public void removeFileActionCommand(String documentUrl, String action) throws GravitonCommunicationException {
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

            try {
                Unirest.put(documentUrl).header("Content-Type", "application/json").body(JSON.std.asString(gravitonFile)).asString();
            } catch (UnirestException | IOException e) {
                throw new GravitonCommunicationException("Unable to remove file action command '" + action + "' from '" + documentUrl + "'.", e);
            }

            LOG.info("Removed action property from " + documentUrl);
        }        
    }
}
