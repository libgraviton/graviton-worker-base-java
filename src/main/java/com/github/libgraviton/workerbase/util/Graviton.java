package com.github.libgraviton.workerbase.util;


import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusInformation;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.file.document.*;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.gdk.GravitonApi;
import com.github.libgraviton.workerbase.gdk.GravitonFileEndpoint;
import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import com.github.libgraviton.workerbase.gdk.exception.CommunicationException;
import com.github.libgraviton.workerbase.helper.DependencyInjection;
import com.github.libgraviton.workerbase.helper.EventStatusHandler;
import com.github.libgraviton.workerbase.helper.WorkerProperties;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Graviton {

    final public static GravitonApi gravitonApi = DependencyInjection.getInstance(GravitonApi.class);
    final public static GravitonFileEndpoint fileEndpoint = DependencyInjection.getInstance(GravitonFileEndpoint.class);
    final public static EventStatusHandler eventStatusHandler = DependencyInjection.getInstance(EventStatusHandler.class);


    public static <T> T getDocumentByUrl(@NotNull String documentUrl, @NotNull Class<T> klass) throws WorkerException {
        try {
            return gravitonApi.get(documentUrl).execute().getBodyItem(klass);
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot get document '%s'.".formatted(documentUrl), e);
        }
    }

    public static <T> List<T> getDocumentsByUrl(@NotNull String documentUrl, @NotNull Class<T> klass) throws WorkerException {
        try {
            return gravitonApi.get(documentUrl).execute().getBodyItems(klass);
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot get document '%s'.".formatted(documentUrl), e);
        }
    }

    public static <T> List<T> getDocumentsByRoute(@NotNull String serviceRoute, @NotNull Class<T> klass) throws WorkerException {
        String url = General.createEndpoint(gravitonApi.getBaseUrl(), serviceRoute);

        try {
            return gravitonApi.get(url).execute().getBodyItems(klass);
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot get document '%s'.".formatted(url), e);
        }
    }

    public static void deleteDocumentByUrl(@NotNull String documentUrl) throws WorkerException {
        try {
            gravitonApi.delete(documentUrl).execute();
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot delete document of '%s'.".formatted(documentUrl), e);
        }
    }

    public static <T> T getDocumentById(@NotNull String id, @NotNull Class<T> klass) throws WorkerException {
        try {
            return gravitonApi.get(id, klass).execute().getBodyItem(klass);
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot get document of type '%s' with id '%s'.".formatted(klass, id), e);
        }
    }

    public static void deleteDocumentById(@NotNull String id, @NotNull Class klass) throws WorkerException {
        try {
            gravitonApi.delete(id, klass).execute();
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot delete document of type '%s' with '%s'.".formatted(klass, id), e);
        }
    }

    public static void deleteDocument(@NotNull GravitonBase resource) throws WorkerException {
        try {
            gravitonApi.delete(resource.getId(), resource.getClass()).execute();
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot delete document of type '%s' with '%s'.".formatted(resource.getClass(), resource.getId()), e);
        }
    }

    public static <T> List<T> getDocumentsByRql(@NotNull String serviceRoute, @NotNull String rql, @NotNull Class<T> klass) throws WorkerException {
        String url = General.createEndpoint(gravitonApi.getBaseUrl(), serviceRoute, rql);

        try {
            return gravitonApi.get(url).execute().getBodyItems(klass);
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot get document of type '%s' by rql '%s'.".formatted(klass, serviceRoute + rql), e);
        }
    }

    public static <T> List<T> getDocumentsByRql(@NotNull String routeWithRql, @NotNull Class<T> klass) throws WorkerException {
        String url = General.createEndpoint(gravitonApi.getBaseUrl(), routeWithRql);

        try {
            return gravitonApi.get(url).execute().getBodyItems(klass);
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot get document of type '%s' by rql '%s'.".formatted(klass, routeWithRql), e);
        }
    }

    public static void writeDocument(GravitonBase resource) throws WorkerException {
        try {
            gravitonApi.post(resource).execute();
        } catch (CommunicationException e) {
            throw new WorkerException(e);
        }
    }

    public static void updateDocument(GravitonBase resource) throws WorkerException {
        try {
            gravitonApi.put(resource).execute();
        } catch (CommunicationException e) {
            throw new WorkerException(e);
        }
    }

    public static byte[] getFileByUrl(@NotNull String documentUrl) throws WorkerException {
        try {
            return fileEndpoint.getFileContentAsBytes(documentUrl);
        } catch (IOException e) {
            throw new WorkerException("Cannot get file '%s'.".formatted(documentUrl), e);
        }
    }

    public static byte[] getFileById(@NotNull String id) throws WorkerException {
        String fileUrl = WorkerProperties.GRAVITON_BASE_URL.get() + "/file/" + id;

        try {
            return fileEndpoint.getFileContentAsBytes(fileUrl);
        } catch (IOException e) {
            throw new WorkerException("Cannot get file '%s'.".formatted(fileUrl), e);
        }
    }

    public static String getAdditionalPropertyFromFile(@NotNull File file, @NotNull String property, boolean throwException) throws WorkerException {
        for (FileMetadataAdditionalProperties prop: file.getMetadata().getAdditionalProperties()) {
            if (prop.getName().equals(property)) {
                return prop.getValue();
            }
        }

        if (throwException) {
            throw new WorkerException("Field '%s' not found in 'metadata.additionalProperties' for file id '%s'.".formatted(property, file.getId()));
        } else {
            return null;
        }
    }

    public static FileMetadataAdditionalProperties getFileMetadataAdditionalProperties(String key, String value) {
        FileMetadataAdditionalProperties prop = new FileMetadataAdditionalProperties();

        prop.setName(key);
        prop.setValue(value);

        return prop;
    }

    public static FileMetadataAction getFileMetadataAction(String command) {
        FileMetadataAction prop = new FileMetadataAction();

        prop.setCommand(command);

        return prop;
    }

    public static List<String> getFileMetadataActions(FileMetadata fileMetadata) {
        if (fileMetadata == null || fileMetadata.getAction().isEmpty()) {
            return new ArrayList<>();
        }

        return fileMetadata.getAction().stream().map(FileMetadataAction::getCommand).toList();
    }

    public static FileLinks getFileLink(String type, String ref) {
        FileLinks link = new FileLinks();

        link.setType(type);
        link.set$ref(ref);

        return link;
    }

    public static EventStatusInformation getInformation(@NotNull EventStatusInformation.Type type, @NotNull String content, String ref, @NotNull String workerId) {
        EventStatusInformation information = new EventStatusInformation();
        information.setWorkerId(workerId);
        information.setType(type);
        information.set$ref(ref);
        information.setContent(content);

        return information;
    }

    public static void updateEventStatus(@NotNull String eventStatusUrl, @NotNull EventStatusStatus.Status status, @NotNull ArrayList<EventStatusInformation> information, @NotNull String workerId) throws WorkerException {
        try {
            EventStatus eventStatus = eventStatusHandler.getEventStatusFromUrl(eventStatusUrl);
            eventStatus.getInformation().addAll(information);
            updateEventStatus(eventStatus, status, workerId);
        } catch (GravitonCommunicationException e) {
            throw new WorkerException("Event status update failed.", e);
        }
    }

    public static void updateEventStatus(@NotNull EventStatus eventStatus, @NotNull EventStatusInformation information) throws WorkerException {
        eventStatus.getInformation().add(information);

        try {
            eventStatusHandler.update(eventStatus);
        } catch (GravitonCommunicationException e) {
            throw new WorkerException("Event status update failed.", e);
        }
    }

    public static void updateEventStatus(@NotNull EventStatus eventStatus, @NotNull EventStatusStatus.Status status, @NotNull EventStatusInformation information, @NotNull String workerId) throws WorkerException {
        eventStatus.getInformation().add(information);
        updateEventStatus(eventStatus, status, workerId);
    }

    public static void updateEventStatus(@NotNull EventStatus eventStatus, @NotNull EventStatusStatus.Status status, @NotNull String workerId) throws WorkerException {
        try {
            eventStatusHandler.update(eventStatus, workerId, status);
        } catch (GravitonCommunicationException e) {
            throw new WorkerException("Event status update failed.", e);
        }
    }

    public static EventStatus getEventStatusById(@NotNull String eventStatusId) throws WorkerException {
        return getEventStatusByRql("?eq(id," + eventStatusId + ")");
    }

    public static EventStatus getEventStatusByRql(@NotNull String filter) throws WorkerException {
        try {
            return eventStatusHandler.getEventStatusByFilter(filter);
        } catch (GravitonCommunicationException e) {
            throw new WorkerException("Unable to fetch event status using filter '%s'.".formatted(filter), e);
        }
    }

    public static void updateFileMetaData(@NotNull File file) throws WorkerException {
        try {
            gravitonApi.put(file).execute();
        } catch (CommunicationException e) {
            throw new WorkerException("Cannot update file metadata for file id '%s'.".formatted(file.getId()), e);
        }
    }

    public static Response updateFile(java.io.File dataFile, @NotNull File file) throws WorkerException {
        try {
            return fileEndpoint.put(dataFile, file).execute();
        } catch (CommunicationException e) {
            throw new WorkerException("Unable to save document.", e);
        }
    }

    public static Response updateFile(byte[] data, @NotNull File file) throws WorkerException {
        try {
            return fileEndpoint.put(data, file).execute();
        } catch (CommunicationException e) {
            throw new WorkerException("Unable to save document.", e);
        }
    }

    public static Response uploadFile(byte[] data, @NotNull File file) throws WorkerException {
       return updateFile(data, file);
    }

    public static Response uploadFile(java.io.File dataFile, @NotNull File file) throws WorkerException {
        return updateFile(dataFile, file);
    }

    public static void removeFileActionCommand(@NotNull File gravitonFile, @NotNull GravitonFileEndpoint fileEndpoint, @NotNull List<String> actions) throws WorkerException {
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
                fileEndpoint.patch(gravitonFile).execute();
            } catch (CommunicationException e) {
                throw new WorkerException("Unable to remove file action elements from '%s'.".formatted(gravitonFile.getId()), e);
            }
        }
    }
}