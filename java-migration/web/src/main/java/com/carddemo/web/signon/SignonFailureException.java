package com.carddemo.web.signon;

import org.springframework.http.HttpStatus;

public class SignonFailureException extends RuntimeException {
    private final HttpStatus status;

    public SignonFailureException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
