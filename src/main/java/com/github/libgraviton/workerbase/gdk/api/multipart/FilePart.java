package com.github.libgraviton.workerbase.gdk.api.multipart;

import java.io.File;

/**
 * Represents a single part of a Multipart request.
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/gdk-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class FilePart {

    private File body;
    private String formName;

    private String contentType = "application/octet-stream";

    public FilePart(File body, String formName) {
        this.body = body;
        this.formName = formName;
    }

    public FilePart(File body, String formName, String contentType) {
        this(body, formName);
        this.contentType = contentType;
    }

    public File getBody() {
        return body;
    }

    public void setBody(File body) {
        this.body = body;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
