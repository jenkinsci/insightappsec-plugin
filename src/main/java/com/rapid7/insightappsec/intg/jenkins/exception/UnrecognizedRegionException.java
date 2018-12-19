package com.rapid7.insightappsec.intg.jenkins.exception;

public class UnrecognizedRegionException extends RuntimeException {

    private static final long serialVersionUID = -679181824198413140L;

    public UnrecognizedRegionException() {
    }

    public UnrecognizedRegionException(String message) {
        super(message);
    }

    public UnrecognizedRegionException(String message,
                                       Throwable cause) {
        super(message, cause);
    }

    public UnrecognizedRegionException(Throwable cause) {
        super(cause);
    }

    public UnrecognizedRegionException(String message,
                                       Throwable cause,
                                       boolean enableSuppression,
                                       boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
