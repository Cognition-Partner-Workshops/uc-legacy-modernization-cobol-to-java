package com.carddemo.exception;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Replaces COBOL's DFHRESP(DUPREC) handling (e.g., in COUSR01C user add).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resourceType, Object resourceId) {
        super(String.format("%s already exists with id: %s", resourceType, resourceId));
    }
}
