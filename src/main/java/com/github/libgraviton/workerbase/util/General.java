package com.github.libgraviton.workerbase.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class General {

    /**
     * extracts the id from an endpoint, e.g:
     * <p>
     * http://localhost:8001/person/customer/10305
     * <p>
     * -> id = 10305
     *
     * @param url $extref endpoint
     * @return id
     */
    public @NotNull static String getIdFromUrl(@NotNull String url) {
        String[] $ref = url.split("/");

        return $ref[$ref.length - 1];
    }

    public @NotNull static String getMimeType(byte[] stream) {
        Tika tika = new Tika();

        return tika.detect(stream);
    }

    public @NotNull static String calculateChecksum(byte[] data) {
        return DigestUtils.sha384Hex(data);
    }

    public static byte[] decodeBase64(byte[] data) {
        // is base64 encoded?
        if (org.apache.commons.codec.binary.Base64.isBase64(data)) {
            return java.util.Base64.getDecoder().decode(data);
        }

        return data;
    }

    /**
     * returns an endpoint with no consecutive slashes in the path.
     *
     * @param hostname hostname
     * @param pathSegments variable list of path segments
     * @return endpoint with no consecutive slashes in the path
     */
    public @NotNull static String createEndpoint(@NotNull String hostname, @NotNull String... pathSegments) {
        ArrayList<String> routeSegments = new ArrayList<>();
        hostname = hostname.replaceAll("/*$", "");

        for (String pathSegment: pathSegments) {
            routeSegments.addAll(Arrays.asList(pathSegment.split("/")));
        }

        routeSegments.removeAll(Collections.singleton(""));

        return hostname + "/" + String.join("/",routeSegments);
    }
}