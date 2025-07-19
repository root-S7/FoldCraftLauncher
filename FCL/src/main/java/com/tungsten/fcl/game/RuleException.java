package com.tungsten.fcl.game;

import java.net.URL;

public class RuleException extends Exception{
    private final URL url;

    public RuleException(URL url) {
        this.url = url;
    }

    public RuleException(String message, URL url) {
        super(message);
        this.url = url;
    }

    public RuleException(String message, Throwable cause, URL url) {
        super(message, cause);
        this.url = url;
    }

    public RuleException(Throwable cause, URL url) {
        super(cause);
        this.url = url;
    }

    public RuleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, URL url) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }
}