/**
 * abstract base class for file workers providing convenience.
 * it extends WorkerAbstract but provides more functions for /file api handling
 */

package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.GravitonFileEndpoint;
import com.github.libgraviton.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadata;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadataAction;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.WorkerUtil;
import com.github.libgraviton.workerbase.model.QueueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected File gravitonFile;

    protected GravitonFileEndpoint fileEndpoint = initFileEndpoint();

    public boolean shouldHandleRequest(QueueEvent queueEvent) throws WorkerException, GravitonCommunicationException {
        // reset gravitonFile, to make sure we have no cached values that will interfere.
        gravitonFile = null;
        String documentUrl = queueEvent.getDocument().get$ref();
        List<String> actions = getActionsOfInterest(queueEvent);

        Boolean actionOfInterestPresent = false;
        for (String action: actions) {
            if (isActionCommandPresent(getGravitonFile(documentUrl), action)) {
                removeFileActionCommand(documentUrl, action);
                actionOfInterestPresent = true;
            }
        }
        return actionOfInterestPresent;
    }

    /**
     * Get required action of interest for worker.
     *
     * @param queueEvent queueEvent
     * @return actions of interest
     */
    public abstract List<String> getActionsOfInterest(QueueEvent queueEvent);

    /**
     * gets file metadata from backend as a GravitonFile object
     *
     * @param fileUrl the url of the object
     * @throws GravitonCommunicationException if file could not be fetched.
     * @return file instance
     */
    public File getGravitonFile(String fileUrl) throws GravitonCommunicationException {
        if (gravitonFile != null && fileUrl.contains(gravitonFile.getId())) {
            return gravitonFile;
        }

        gravitonFile = WorkerUtil.getGravitonFile(fileEndpoint, fileUrl);
        return gravitonFile;
    }    
    
    /**
     * checks if a certain action is present in the metadata.action array
     *
     * @param gravitonFile a {@link File} object.
     * @param action a {@link java.lang.String} object.
     * @return true if yes, false if not
     */
    public Boolean isActionCommandPresent(File gravitonFile, String action) {
        FileMetadata metadata = gravitonFile.getMetadata();
        for (FileMetadataAction singleAction: metadata.getAction()) {
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
        gravitonFile = null;
        gravitonFile = getGravitonFile(documentUrl);
        FileMetadata metadata = gravitonFile.getMetadata();
        List<FileMetadataAction> metadataActions = metadata.getAction();
        List<FileMetadataAction> matchingActions = new ArrayList<>();

        for (FileMetadataAction metadataAction : metadataActions) {
            if (action.equals(metadataAction.getCommand())) {
                matchingActions.add(metadataAction);
            }
        }

        if (matchingActions.size() > 0) {
            metadataActions.removeAll(matchingActions);

            try {
                fileEndpoint.patch(gravitonFile).execute();
            } catch (CommunicationException e) {
                throw new GravitonCommunicationException("Unable to remove file action command '" + action + "' from '" + documentUrl + "'.", e);
            }

            LOG.info("Removed action property from " + documentUrl);
        }        
    }

    protected GravitonFileEndpoint initFileEndpoint() {
        return new GravitonFileEndpoint(gravitonApi);
    }
}
