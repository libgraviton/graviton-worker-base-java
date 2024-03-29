package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.gravitondyn.file.document.File;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadata;
import com.github.libgraviton.gdk.gravitondyn.file.document.FileMetadataAction;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.QueueEventScope;
import com.github.libgraviton.workerbase.helper.WorkerScope;
import com.github.libgraviton.workerbase.model.QueueEvent;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>Abstract FileWorkerAbstract class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public abstract class FileQueueWorkerAbstract extends QueueWorkerAbstract implements FileQueueWorkerInterface {

    private static final Logger LOG = LoggerFactory.getLogger(FileQueueWorkerAbstract.class);

    public FileQueueWorkerAbstract(WorkerScope workerScope) {
        super(workerScope);
    }

    public boolean shouldHandleRequest(QueueEventScope queueEventScope) throws GravitonCommunicationException {
        QueueEvent queueEvent = queueEventScope.getQueueEvent();
        List<String> actions = getActionsOfInterest(queueEvent);

        // get the file
        File currentFile = queueEventScope.getFileEndpoint().getFileFromQueueEvent(queueEvent);

        queueEventScope.getScopeCacheMap().put("currentFetchedFile", currentFile);

        if (currentFile == null) {
            // something is off..
            return false;
        }

        FileMetadata fileMetadata = currentFile.getMetadata();

        if (fileMetadata == null || fileMetadata.getAction().isEmpty()) {
            // no actions defined..
            return false;
        }

        // get all actions in the file
        List<String> referencedActions = fileMetadata.getAction().stream().map(FileMetadataAction::getCommand).toList();

        // any intersect? if so, we should handle it.
        List<String> matching = ListUtils.intersection(actions, referencedActions);

        if (matching.isEmpty()) {
            return false;
        }

        // something matches. clear the actions from the files first.
        removeFileActionCommand(currentFile, matching, queueEventScope);

        return !ListUtils.intersection(actions, referencedActions).isEmpty();
    }

    final public void handleRequest(QueueEventScope queueEventScope) throws WorkerException, GravitonCommunicationException {
        // get the file..
        File fileToPass;
        if (queueEventScope.getScopeCacheMap().containsKey("currentFetchedFile")) {
            fileToPass = (File) queueEventScope.getScopeCacheMap().get("currentFetchedFile");
        } else {
            fileToPass = queueEventScope.getFileEndpoint().getFileFromQueueEvent(queueEventScope.getQueueEvent());
        }

        handleFileRequest(fileToPass, queueEventScope);
    }

    /**
     * Get required action of interest for worker.
     *
     * @param queueEvent queueEvent
     * @return actions of interest
     */
    public abstract List<String> getActionsOfInterest(QueueEvent queueEvent);

    /**
     * checks if a certain action is present in the metadata.action array
     *
     * @param gravitonFile a {@link File} object.
     * @param action a {@link java.lang.String} object.
     * @return true if yes, false if not
     */
    public boolean isActionCommandPresent(File gravitonFile, String action) {
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
     * @throws GravitonCommunicationException when file action command could not be removed at Graviton
     */
    private void removeFileActionCommand(File gravitonFile, List<String> actions, QueueEventScope queueEventScope) throws GravitonCommunicationException {
        FileMetadata metadata = gravitonFile.getMetadata();

        // get the matching ones
        List<FileMetadataAction> matchingActions = metadata
                .getAction()
                .stream()
                .filter(s -> actions.contains(s.getCommand())).toList();

        if (matchingActions.size() > 0) {
            // remove them
            gravitonFile.getMetadata().getAction().removeAll(matchingActions);

            try {
                queueEventScope.getFileEndpoint().patch(gravitonFile).execute();
            } catch (CommunicationException e) {
                throw new GravitonCommunicationException("Unable to remove file action elements from '" + gravitonFile.getId() + "'.", e);
            }

            LOG.info("Removed action elements '{}' property from file ID '{}'", actions.toArray(), gravitonFile.getId());
        }        
    }
}
