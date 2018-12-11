package com.rapid7.insightappsec.intg.jenkins.exception;

public class VulnerabilitiesPresentException extends RuntimeException {

    private static final long serialVersionUID = 6479409439957909618L;

    public VulnerabilitiesPresentException() {
    }

    public VulnerabilitiesPresentException(String message) {
        super(message);
    }

    public VulnerabilitiesPresentException(String message,
                                           Throwable cause) {
        super(message, cause);
    }

    public VulnerabilitiesPresentException(Throwable cause) {
        super(cause);
    }

    public VulnerabilitiesPresentException(String message,
                                           Throwable cause,
                                           boolean enableSuppression,
                                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
