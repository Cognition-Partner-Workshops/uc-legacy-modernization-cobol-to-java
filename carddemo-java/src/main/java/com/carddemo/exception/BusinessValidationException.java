package com.carddemo.exception;

/**
 * Exception for business rule violations.
 * Replaces COBOL's WS-ERR-FLG pattern and inline validation error messages.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
