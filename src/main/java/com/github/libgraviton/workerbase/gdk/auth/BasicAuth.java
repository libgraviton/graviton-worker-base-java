package com.github.libgraviton.workerbase.gdk.auth;

import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.auth.HeaderAuth;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class BasicAuth implements HeaderAuth {

    private static final Logger LOG = LoggerFactory.getLogger(BasicAuth.class);

    private final String username;
    private final String password;

    public BasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void addHeader(HeaderBag.Builder headerBuilder) {
        LOG.debug("Add basic auth header to request");
        headerBuilder.set("Authorization", basic());
    }

    public String basic() {
        return basic(StandardCharsets.ISO_8859_1);
    }

    public String basic(Charset charset) {
        String usernameAndPassword = username + ":" + password;
        byte[] bytes = usernameAndPassword.getBytes(charset);
        String encoded = ByteString.of(bytes).base64();
        return "Basic ".concat(encoded);
    }
}
