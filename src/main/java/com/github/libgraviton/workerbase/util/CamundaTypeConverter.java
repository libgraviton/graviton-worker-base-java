package com.github.libgraviton.workerbase.util;

import org.bson.Document;


public class CamundaTypeConverter {
    final private static String BOOLEAN = "boolean";
    final private static String SHORT = "short";
    final private static String INTEGER = "integer";
    final private static String LONG = "long";
    final private static String STRING = "string";
    final private static String NULL = "null";


    private static String getType(Document doc) {
        return doc.getString("type").toLowerCase();
    }

    public static String getString(Document doc) {
        String type = getType(doc);

        switch (type) {
            case NULL -> {
                return null;
            }
            case BOOLEAN -> {
                return String.valueOf(doc.getBoolean("value"));
            }
            case INTEGER, SHORT -> {
                return String.valueOf(doc.getInteger("value"));
            }
            case LONG -> {
                try {
                    return String.valueOf(doc.getLong("value"));
                } catch (Exception e) {
                    return String.valueOf(doc.getInteger("value"));
                }
            }
            default -> {
                // json, xml, date,...
                return doc.getString("value");
            }
        }
    }

    public static Integer getInteger(Document doc) {
        String type = getType(doc);

        return switch (type) {
            case NULL -> null;
            case BOOLEAN -> doc.getBoolean("value") ? 1 : 0;
            case INTEGER, SHORT -> doc.getInteger("value");
            case LONG -> throw new NumberFormatException("Long type cannot be converted to int without losing precision.");
            case STRING -> Integer.valueOf(doc.getString("value"));
            default -> throw new NumberFormatException("Unknown/Unhandled BPF type: " + type);
        };
    }

    public static Long getLong(Document doc) {
        String type = getType(doc);

        return switch (type) {
            case NULL -> null;
            case BOOLEAN -> (long) (doc.getBoolean("value") ? 1 : 0);
            case SHORT, INTEGER -> Long.valueOf(doc.getInteger("value"));
            case LONG -> doc.getLong("value");
            case STRING -> Long.valueOf(doc.getString("value"));
            default -> throw new NumberFormatException("Unknown/Unhandled BPF type: " + type);
        };
    }
}