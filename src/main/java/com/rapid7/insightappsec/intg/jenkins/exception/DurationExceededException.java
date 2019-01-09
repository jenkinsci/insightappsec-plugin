package com.rapid7.insightappsec.intg.jenkins.exception;

public class DurationExceededException extends RuntimeException {

    private static final long serialVersionUID = 6479409439957909618L;

    public DurationExceededException() {
    }

    public DurationExceededException(String message) {
        super(message);
    }

    public DurationExceededException(String message,
                                     Throwable cause) {
        super(message, cause);
    }

    public DurationExceededException(Throwable cause) {
        super(cause);
    }

    public DurationExceededException(String message,
                                     Throwable cause,
                                     boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
