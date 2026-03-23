package com.carddemo.exception;

/**
 * Thrown when authentication fails.
 * Replaces COBOL COSGN00C password mismatch and user-not-found error handling.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
