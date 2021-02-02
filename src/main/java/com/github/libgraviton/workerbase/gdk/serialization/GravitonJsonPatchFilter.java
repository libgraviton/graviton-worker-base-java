package com.github.libgraviton.workerbase.gdk.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Since Graviton uses the library https://github.com/raphaelstolt/php-jsonpatch that doesn't strongly conform to RFC 6902,
 * we need to make sure that the JSON Patch request matches its needs.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class GravitonJsonPatchFilter {

    /**
     * Turns this
     *
     * <pre>
     *[
     *  {
     *    "op": "replace",
     *    "path": "/status/0/status",
     *    "value": "failed"
     *  },
     *  {
     *    "op": "add",
     *    "path": "/information/0",
     *    "value": "aValue"
     *  }
     *]
     * </pre>
     *
     * into this
     *
     * <pre>
     *[
     *  {
     *    "op": "replace",
     *    "path": "/status/0/status",
     *    "value": "failed"
     *  },
     *  {
     *    "op": "add",
     *    "path": "/information/-",
     *    "value": "aValue"
     *  }
     *]
     * </pre>
     *
     * @param patch prepared Json Patch Diff that will be filtered on
     * @return modified JsonNode patch
     */
    public JsonNode filter(JsonNode patch) {
        // make sure we add new list elements as '/-' instead of '/0' since the graviton library only supports it that way
        for (JsonNode node : patch) {
            if ("add".equals(node.get("op").textValue())) {
                ((ObjectNode) node).put("path", node.get("path").textValue().replaceAll("/[0-9]+$", "/-"));
            }
        }
        return patch;
    }
}
