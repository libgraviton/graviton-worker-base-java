package com.github.libgraviton.workerbase.gdk.api.query.rql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.libgraviton.workerbase.gdk.api.query.Query;
import com.github.libgraviton.workerbase.gdk.api.query.QueryStatement;
import com.github.libgraviton.workerbase.gdk.api.query.rql.statements.AndOperator;
import com.github.libgraviton.workerbase.gdk.api.query.rql.statements.Eq;
import com.github.libgraviton.workerbase.gdk.api.query.rql.statements.Limit;
import com.github.libgraviton.workerbase.gdk.api.query.rql.statements.Select;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Together with the enclosed Builder, it allows to generate RQL queries that can be used for GET requests.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class Rql extends Query {

    private static final Logger LOG = LoggerFactory.getLogger(com.github.libgraviton.workerbase.gdk.api.query.rql.Rql.class);

    public static final String DEFAULT_ENCODING = "UTF-8";

    private Rql(List<QueryStatement> statements) {
        this.statements = statements;
    }

    /**
     * RQL encoding string according to https://github.com/xiag-ag/rql-parser
     *
     * @param input string to encode
     * @param encoding encoding
     * @return RQL encoded string
     * @throws UnsupportedEncodingException if the named encoding is not supported
     */
    public static String encode(String input, String encoding) throws UnsupportedEncodingException {
        return URLEncoder.encode(input, encoding)
                .replace("-", "%2D")
                .replace("_", "%5F")
                .replace(".", "%2E")
                .replace("~", "%7E");
    }

    public static class Builder {

        private Limit limit;

        private Select select;

        private QueryStatement resourceStatement;

        public com.github.libgraviton.workerbase.gdk.api.query.rql.Rql.Builder setLimit(int numberOfElements, int offset) {
            limit = new Limit(numberOfElements, offset);
            return this;
        }

        public com.github.libgraviton.workerbase.gdk.api.query.rql.Rql.Builder setLimit(int numberOfElements) {
            limit = new Limit(numberOfElements);
            return this;
        }

        public com.github.libgraviton.workerbase.gdk.api.query.rql.Rql.Builder addSelect(String attributeName) {
            if (select == null) {
                select = new Select();
            }

            select.add(attributeName);

            return this;
        }

        /**
         * Creates an QueryStatement for each attribute set in the passed 'resource' parameter.
         * If there are more than 1 QueryStatement, they will be AND connected.
         *
         * @param resource resource to generate the statements from
         * @param mapper ObjectMapper
         * @return resulting QueryStatement
         */
        public com.github.libgraviton.workerbase.gdk.api.query.rql.Rql.Builder setResource(GravitonBase resource, ObjectMapper mapper) {
            JsonNode node = mapper.valueToTree(resource);

            List<QueryStatement> queryStatements = getQueryStatementsFromNode(node, mapper.getDateFormat(), null);
            switch (queryStatements.size()) {
                case 0:
                    resourceStatement = null;
                    break;
                case 1:
                    resourceStatement = queryStatements.get(0);
                    break;
                default:
                    AndOperator andOperator = new AndOperator();
                    andOperator.addStatements(queryStatements);
                    resourceStatement = andOperator;
                    break;
            }

            return this;
        }

        public com.github.libgraviton.workerbase.gdk.api.query.rql.Rql build() {
            List<QueryStatement> statements = new ArrayList<>();
            if (resourceStatement != null) {
                statements.add(resourceStatement);
            }
            if (limit != null) {
                statements.add(limit);
            }
            if (select != null) {
                statements.add(select);
            }
            return new com.github.libgraviton.workerbase.gdk.api.query.rql.Rql(statements);
        }

        protected List<QueryStatement> getQueryStatementsFromNode(JsonNode node, DateFormat dateFormat, String path) {
            List<QueryStatement> statements = new ArrayList<>();
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                String currentPath = path == null ? fieldName : path + "." + fieldName;
                JsonNode currentNode = node.get(fieldName);
                if (currentNode.isArray()) {
                    for (JsonNode nodeEntry : currentNode) {
                        statements.addAll(getQueryStatementsFromNode(nodeEntry, dateFormat, currentPath + "."));
                    }
                } else if (currentNode.isObject()) {
                    statements.addAll(getQueryStatementsFromNode(currentNode, dateFormat, currentPath));
                } else {
                    String value = currentNode.textValue();
                    // whenever the value is a parsable date, the 'string:' prefix needs to be omitted because
                    // searching date fields as 'string:' will not result in a match when used in a RQL query.
                    try {
                        dateFormat.parse(value);
                    } catch (ParseException e) {
                        try {
                            value = encode(value, DEFAULT_ENCODING);
                        } catch (UnsupportedEncodingException uee) {
                            LOG.warn("Unsupported encoding '" + DEFAULT_ENCODING + "', using unencoded value '" + value + "'.");
                        }
                        value = "string:" + value;
                    }

                    Eq eq = new Eq(currentPath, value);
                    statements.add(eq);
                }
            }

            return statements;
        }
    }
}
