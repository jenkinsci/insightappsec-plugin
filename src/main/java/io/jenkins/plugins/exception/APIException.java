package io.jenkins.plugins.exception;

import org.apache.http.HttpResponse;

public class APIException extends RuntimeException {

    private static final long serialVersionUID = 3200321158057334737L;

    private HttpResponse response;

    public APIException() {
    }

    public APIException(String message,
                        HttpResponse response) {
        super(message);
        this.response = response;
    }

    public APIException(String message,
                        Throwable cause) {
        super(message, cause);
    }

    public HttpResponse getResponse() {
        return response;
    }
}
