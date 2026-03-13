package com.carddemo.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Replaces COBOL "RECORD NOT FOUND" conditions across VSAM file operations.
 */
public class ResourceNotFoundException extends CardDemoException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Object id) {
        super(String.format("%s not found with id: %s", resourceType, id));
    }
}
