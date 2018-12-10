package com.rapid7.insightappsec.intg.jenkins.exception;

public class ScanSubmissionFailedException extends RuntimeException {

    private static final long serialVersionUID = 8656959210418165704L;

    public ScanSubmissionFailedException() {
    }

    public ScanSubmissionFailedException(String message) {
        super(message);
    }

    public ScanSubmissionFailedException(String message,
                                         Throwable cause) {
        super(message, cause);
    }

    public ScanSubmissionFailedException(Throwable cause) {
        super(cause);
    }

    public ScanSubmissionFailedException(String message,
                                         Throwable cause,
                                         boolean enableSuppression,
                                         boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
