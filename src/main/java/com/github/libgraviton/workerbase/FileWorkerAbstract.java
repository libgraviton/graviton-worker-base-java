/**
 * abstract base class for file workers providing convenience.
 * it extends WorkerAbstract but provides more functions for /file api handling
 */

package com.github.libgraviton.workerbase;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import com.github.libgraviton.workerbase.model.QueueEvent;
import com.github.libgraviton.workerbase.model.file.GravitonFile;
import com.github.libgraviton.workerbase.model.file.Metadata;
import com.github.libgraviton.workerbase.model.file.MetadataAction;
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

    private GravitonFile gravitonFile;

    public boolean shouldHandleRequest(QueueEvent queueEvent) throws WorkerException {
        String documentUrl = queueEvent.getDocument().get$ref();
        String action = getAction(queueEvent);

        try {
            if (!isActionCommandPresent(getGravitonFile(documentUrl), action)) {
                return false;
            }
            removeFileActionCommand(documentUrl, action);
            return true;
        } catch (GravitonCommunicationException e) {
            throw new WorkerException("Could not remove action '" + action + "' at url '" + documentUrl + "'.", e);
        }
    }

    /**
     * Get required action for worker.
     *
     * @param queueEvent queueEvent
     * @return action of interest
     */
    public abstract String getAction(QueueEvent queueEvent);

    /**
     * gets file metadata from backend as a GravitonFile object
     *
     * @param fileUrl the url of the object
     * @throws GravitonCommunicationException if file could not be fetched.
     * @return file instance
     */
    public GravitonFile getGravitonFile(String fileUrl) throws GravitonCommunicationException {
        if (gravitonFile != null && fileUrl.contains(gravitonFile.getId())) {
            return gravitonFile;
        }

        gravitonFile = WorkerUtil.getGravitonFile(fileUrl);
        return gravitonFile;
    }    
    
    /**
     * checks if a certain action is present in the metadata.action array
     *
     * @param gravitonFile a {@link GravitonFile} object.
     * @param action a {@link java.lang.String} object.
     * @return true if yes, false if not
     */
    public Boolean isActionCommandPresent(GravitonFile gravitonFile, String action) {
        Metadata metadata = gravitonFile.getMetadata();
        for (MetadataAction singleAction: metadata.getAction()) {
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
        gravitonFile = null;
        gravitonFile = getGravitonFile(documentUrl);
        Metadata metadata = gravitonFile.getMetadata();
        List<MetadataAction> metadataActions = metadata.getAction();
        List<MetadataAction> matchingActions = new ArrayList<>();

        for (MetadataAction metadataAction : metadataActions) {
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
