package com.rapid7.insightappsec.intg.jenkins.exception;

import org.apache.http.HttpResponse;

public class APIException extends RuntimeException {

    private static final long serialVersionUID = 8656959210418165704L;

    private HttpResponse response;

    public APIException() {
    }

    public APIException(String message) {
        super(message);
    }

    public APIException(String message, HttpResponse response) {
        super(message);
        this.response = response;
    }

    public APIException(String message,
                        Throwable cause) {
        super(message, cause);
    }

    public APIException(Throwable cause) {
        super(cause);
    }

    public APIException(String message,
                        Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public HttpResponse getResponse() {
        return response;
    }
}
