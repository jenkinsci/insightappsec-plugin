package com.rapid7.insightappsec.intg.jenkins.exception;

public class WaitTimeExceededException extends RuntimeException {

    private static final long serialVersionUID = 6479409439957909618L;

    public WaitTimeExceededException() {
    }

    public WaitTimeExceededException(String message) {
        super(message);
    }

    public WaitTimeExceededException(String message,
                                     Throwable cause) {
        super(message, cause);
    }

    public WaitTimeExceededException(Throwable cause) {
        super(cause);
    }

    public WaitTimeExceededException(String message,
                                     Throwable cause,
                                     boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
