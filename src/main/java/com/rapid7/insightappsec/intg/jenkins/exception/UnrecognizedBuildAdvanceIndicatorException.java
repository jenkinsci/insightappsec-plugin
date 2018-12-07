package com.rapid7.insightappsec.intg.jenkins.exception;

public class UnrecognizedBuildAdvanceIndicatorException extends RuntimeException {

    private static final long serialVersionUID = -679181824198413140L;

    public UnrecognizedBuildAdvanceIndicatorException() {
    }

    public UnrecognizedBuildAdvanceIndicatorException(String message) {
        super(message);
    }

    public UnrecognizedBuildAdvanceIndicatorException(String message,
                                                      Throwable cause) {
        super(message, cause);
    }

    public UnrecognizedBuildAdvanceIndicatorException(Throwable cause) {
        super(cause);
    }

    public UnrecognizedBuildAdvanceIndicatorException(String message,
                                                      Throwable cause,
                                                      boolean enableSuppression,
                                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
