package com.carddemo.reference.exception;

/**
 * Thrown when a requested transaction type or category does not exist.
 *
 * <p>Corresponds to the {@code SQLCODE +100} (row not found) handling in the
 * legacy DB2 programs. Surfaces as HTTP 404 Not Found.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
