package com.github.libgraviton.workerbase.gdk.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonDiff;
import com.github.libgraviton.workerbase.gdk.serialization.GravitonJsonPatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Keeps track of POJOs there were deserialized via Response (with its JsonNode in its original form),
 * so that the JSON Patch Diff can be calculated between the original and the current object state.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class JsonPatcher {

    private JsonPatcher() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.gdk.serialization.JsonPatcher.class);

    // Since we use WeakHashMap, we don't need to worry about housekeeping.
    // Whenever a map key is no longer referenced, the entry will automatically be removed from this map.
    private static Map<Object, JsonNode> memory = new WeakHashMap<>();

    /**
     * Adds the 'reference' object and its JsonNode representation as 'original' to the patch memory.
     *
     * @param reference object that need to keep track of for possible future PATCH requests
     * @param original JsonNode representation of the 'reference'
     */
    public static void add(Object reference, JsonNode original) {
        memory.put(reference, original);
        LOG.debug("Added new entry of type '" + reference.getClass() + "'. Currently maintaining '" + memory.size() + "' entries.");
    }

    /**
     * Use the 'reference' object to fetch its original state from the patch memory and create a Diff Patch.
     *
     * @param reference object used to fetch the original state from the patch memory
     * @param current JsonNode representation of the 'reference'
     * @return Diff Patch between 'current' and the original JsonNode of this object
     */
    public static JsonNode getPatch(Object reference, JsonNode current) {
        JsonNode original = memory.get(reference);
        if(original == null) {
            return null;
        }
        JsonNode patch = JsonDiff.asJson(original, current);
        return getFilter().filter(patch);
    }

    protected static GravitonJsonPatchFilter getFilter() {
        return new GravitonJsonPatchFilter();
    }

    protected static Map<Object, JsonNode> getMemory() {
        return memory;
    }

    protected static void clearMemory() {
        memory.clear();
    }
}
