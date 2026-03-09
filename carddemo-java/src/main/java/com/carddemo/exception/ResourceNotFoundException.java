package com.carddemo.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Replaces COBOL VSAM response code 13 (record not found) handling.
 * Example from COSGN00C: WHEN 13 -> 'User not found. Try again ...'
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
