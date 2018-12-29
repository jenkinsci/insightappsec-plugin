package com.rapid7.insightappsec.intg.jenkins.exception;

public class ScanAPIFailureException extends RuntimeException {

    private static final long serialVersionUID = 8656959210418165704L;

    public ScanAPIFailureException() {
    }

    public ScanAPIFailureException(String message) {
        super(message);
    }

    public ScanAPIFailureException(String message,
                                   Throwable cause) {
        super(message, cause);
    }

    public ScanAPIFailureException(Throwable cause) {
        super(cause);
    }

    public ScanAPIFailureException(String message,
                                   Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
