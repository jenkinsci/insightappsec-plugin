package com.rapid7.insightappsec.intg.jenkins.exception;

public class ScanFailureException extends RuntimeException {

    private static final long serialVersionUID = 6479409439957909618L;

    public ScanFailureException() {
    }

    public ScanFailureException(String message) {
        super(message);
    }

    public ScanFailureException(String message,
                                Throwable cause) {
        super(message, cause);
    }

    public ScanFailureException(Throwable cause) {
        super(cause);
    }

    public ScanFailureException(String message,
                                Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
