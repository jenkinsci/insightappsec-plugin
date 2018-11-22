package com.rapid7.insightappsec.intg.jenkins.exception;

public class ScanRetrievalFailedException extends RuntimeException {

    private static final long serialVersionUID = 6479409439957909618L;

    public ScanRetrievalFailedException() {
    }

    public ScanRetrievalFailedException(String message) {
        super(message);
    }

    public ScanRetrievalFailedException(String message,
                                        Throwable cause) {
        super(message, cause);
    }

    public ScanRetrievalFailedException(Throwable cause) {
        super(cause);
    }

    public ScanRetrievalFailedException(String message,
                                        Throwable cause,
                                        boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
