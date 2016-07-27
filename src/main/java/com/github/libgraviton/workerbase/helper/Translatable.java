package com.github.libgraviton.workerbase.helper;

/**
 * Represents a /i18n/translatable
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public class Translatable {
    private String locale;
    private String translated;

    public String getLocale() {
        return locale;
    }

    public String getTranslated() {
        return translated;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setTranslated(String translated) {
        this.translated = translated;
    }
}
