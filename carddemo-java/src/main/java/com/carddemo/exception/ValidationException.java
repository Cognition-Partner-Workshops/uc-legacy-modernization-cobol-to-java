package com.carddemo.exception;

/**
 * Exception thrown when input validation fails.
 * Replaces COBOL input validation patterns from online programs
 * (e.g., COACTVWC checking for blank/zero account numbers).
 */
public class ValidationException extends RuntimeException {

    private final int failureCode;

    public ValidationException(String message) {
        super(message);
        this.failureCode = 0;
    }

    public ValidationException(int failureCode, String message) {
        super(message);
        this.failureCode = failureCode;
    }

    public int getFailureCode() {
        return failureCode;
    }
}
