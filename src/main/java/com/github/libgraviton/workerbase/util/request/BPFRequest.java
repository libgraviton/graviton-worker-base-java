package com.github.libgraviton.workerbase.util.request;

import com.github.libgraviton.workerbase.util.Converter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;


public class BPFRequest extends GatewayRequest {

    public BPFRequest() {
        super();
    }

    public Document getDocument(@NotNull Document body) throws Exception {
        String json = execute(body);


        if (json == null || json.trim().equals("[]")) {
            return null;
        } else if (json.trim().startsWith("[")) {
            Document result = new Document();

            for (Map.Entry<String, Object> entry: Converter.getInstance(json, Document[].class)[0].entrySet()) {
                if (entry.getValue() instanceof LinkedHashMap) {
                    Document d = new Document();
                    d.putAll((LinkedHashMap)entry.getValue());
                    result.put(entry.getKey(), d);
                } else {
                    result.put(entry.getKey(), entry.getValue());
                }
            }

            return result;
        } else {
            return Converter.getInstance(json, Document.class);
        }
    }

    public Document getDocument() throws Exception {
        return getDocument(new Document());
    }

    public Document[] getDocumentArray(@NotNull Document body) throws Exception {
        return Converter.getInstance(execute(body), Document[].class);
    }

    public Document[] getDocumentArray() throws Exception {
        return getDocumentArray(new Document());
    }

    private String execute(Document body) throws Exception {
        return this.setBody(body.toJson()).execute().getBody();
    }
}