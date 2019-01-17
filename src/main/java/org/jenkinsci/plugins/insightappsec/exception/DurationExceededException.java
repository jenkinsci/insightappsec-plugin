package org.jenkinsci.plugins.insightappsec.exception;

public class DurationExceededException extends RuntimeException {

    private static final long serialVersionUID = 8773830237854364135L;

    public DurationExceededException() {
        super("Max scan pending duration has been exceeded");
    }

}
