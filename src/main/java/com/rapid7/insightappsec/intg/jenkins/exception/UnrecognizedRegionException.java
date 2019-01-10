package com.rapid7.insightappsec.intg.jenkins.exception;

public class UnrecognizedRegionException extends RuntimeException {

    private static final long serialVersionUID = 4881614996564139207L;

    public UnrecognizedRegionException(String region) {
        super(String.format("The region provided [%s] is not recognized", region));
    }

}
