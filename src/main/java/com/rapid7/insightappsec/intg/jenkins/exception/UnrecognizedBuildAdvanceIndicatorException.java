package com.rapid7.insightappsec.intg.jenkins.exception;

public class UnrecognizedBuildAdvanceIndicatorException extends RuntimeException {

    private static final long serialVersionUID = -679181824198413140L;

    public UnrecognizedBuildAdvanceIndicatorException(String buildAdvanceIndicator) {
        super(String.format("The build advance indicator provided [%s] is not recognized", buildAdvanceIndicator));
    }

}
