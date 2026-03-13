package com.carddemo.common.exception;

/**
 * Exception thrown when validation fails.
 * Replaces COBOL validation error handling (e.g., invalid dates, overlimit checks).
 */
public class ValidationException extends CardDemoException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String field, String reason) {
        super(String.format("Validation failed for field '%s': %s", field, reason));
    }
}
