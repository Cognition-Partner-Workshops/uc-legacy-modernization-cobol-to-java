package com.carddemo.exception;

/**
 * Thrown when business validation rules are violated.
 * Replaces COBOL input validation patterns (e.g., WS-ERR-FLG checks,
 * FLG-ACCTFILTER-NOT-OK conditions in COACTVWC).
 */
public class BusinessValidationException extends RuntimeException {
    public BusinessValidationException(String message) {
        super(message);
    }
}
