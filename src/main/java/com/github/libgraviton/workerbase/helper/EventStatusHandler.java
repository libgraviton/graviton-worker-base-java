package com.github.libgraviton.workerbase.helper;

import com.fasterxml.jackson.jr.ob.JSON;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.model.status.EventStatus;
import com.github.libgraviton.workerbase.model.status.WorkerStatus;
import com.github.libgraviton.workerbase.model.status.Status;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Modify EventStatus entries at Graviton
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public class EventStatusHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EventStatusHandler.class);

    private String eventStatusBaseUrl;

    public EventStatusHandler(String eventStatusBaseUrl) {
        this.eventStatusBaseUrl = eventStatusBaseUrl;
    }

    /**
     * Update the event status object on Graviton side
     *
     * @param eventStatus adapted status that should be updated
     * @param workerId only the status of this workerId will be updated
     * @param workerStatus the new status of the worker
     * @throws GravitonCommunicationException when status cannot be updated at Graviton
     */
    public void update(EventStatus eventStatus, String workerId, Status workerStatus) throws GravitonCommunicationException {

        List<WorkerStatus> status = eventStatus.getStatus();

        if (status == null) {
            throw new IllegalStateException("Got an invalid EventStatus status.");
        }

        for (WorkerStatus statusEntry : status) {
            String currentWorkerId = statusEntry.getWorkerId();
            if(currentWorkerId != null && currentWorkerId.equals(workerId)) {
                statusEntry.setStatus(workerStatus);
            }
        }
        HttpResponse updateResponse;
        HttpRequestWithBody updateRequest = Unirest.put(eventStatusBaseUrl + eventStatus.getId())
                .header("Content-Type", "application/json");
        String statusUrl = updateRequest.getUrl();
        try {
            updateResponse = updateRequest.body(JSON.std.asString(eventStatus)).asString();
        } catch (IOException | UnirestException e) {
            throw new GravitonCommunicationException("Failed to update the event status on '" + statusUrl + "'.", e);
        }

        if (updateResponse.getStatus() != 204) {
            throw new GravitonCommunicationException("Failed to update the event status on '" + statusUrl + "'. Return status was '" + updateResponse.getStatus() + "'");
        }

        LOG.info("Updated status to '" + workerStatus + "' on '" + statusUrl + "'.");
    }

    public EventStatus getEventStatusFromUrl(String url) throws GravitonCommunicationException {
        try {
            HttpResponse response = Unirest.get(url).header("Accept", "application/json").asString();
            return JSON.std.beanFrom(EventStatus.class, response.getBody());
        } catch (UnirestException | IOException e) {
            throw new GravitonCommunicationException("Failed to GET event status from '" + url + "'.", e);
        }
    }

    /**
     * Retrieve an EventStatus by a certain RQL filter.
     *
     * @param filter RQL filter
     * @return EventStatus matching the criteria
     * @throws GravitonCommunicationException when there are less or more than 1 EventStatus match.
     */
    public EventStatus getEventStatusByFilter(String filter) throws GravitonCommunicationException {
        JSONArray statusDocuments = findEventStatus(filter);
        int statusCount = statusDocuments.length();

        if (statusCount == 0) {
            throw new GravitonCommunicationException("No corresponding event status found for filter '" + filter + "'.");
        }

        if (statusCount > 1) {
            throw new GravitonCommunicationException("Multiple event status matches found for filter '" + filter + "'.");
        }

        try {
            return JSON.std.beanFrom(EventStatus.class, statusDocuments.get(0).toString());
        } catch (IOException e) {
            throw new GravitonCommunicationException("Invalid event status found for filter '" + filter + "'.", e);
        }
    }

    /**
     * Finds all event status documents which are found by a given RQL filter.
     *
     * @param filter RQL filter that gets attached to the eventStatusBaseUrl
     *
     * @return All found event status documents
     *
     * @throws GravitonCommunicationException when Event Status cannot be retrieved from Graviton
     */
    public JSONArray findEventStatus(String filter) throws GravitonCommunicationException {
        // TODO refactor from JSONArray to List<EventStatus>
        try {
            return Unirest.get(eventStatusBaseUrl + filter)
                    .header("Accept", "application/json")
                    .asJson()
                    .getBody()
                    .getArray();
        } catch (UnirestException e) {
            throw new GravitonCommunicationException("Could not GET matching EventStatus for filter '" + filter + "'.", e);
        }
    }

    /**
     * Creates an RQL filter by replacing all placeholder in the form {placeholder} from the rqlTemplate.
     *
     * @param rqlTemplate template with placeholders
     * @param replacements placeholder replacements
     * @return ready to use RQL filter
     */
    public String getRqlFilter(String rqlTemplate, String... replacements) {
        // placeholder -> {somePlaceholder}
        String placeholderRegex = "\\{[^\\{]*\\}";

        for (String replacement : replacements) {
            replacement = replacement.replaceAll("-", "%2D");
            rqlTemplate = rqlTemplate.replaceFirst(placeholderRegex, replacement);
        }
        return rqlTemplate;
    }
}
