package com.carddemo.exception;

/**
 * Thrown when a requested resource is not found.
 * Replaces COBOL RESP code 13 (NOTFND) handling in CICS READ operations.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
