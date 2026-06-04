package com.carddemo.reference.exception;

/**
 * Thrown when creating a transaction type or category whose key already
 * exists.
 *
 * <p>Corresponds to the {@code SQLCODE -803} (duplicate key) handling in the
 * legacy DB2 INSERT paths of {@code COTRTUPC} / {@code COBTUPDT}. Surfaces as
 * HTTP 409 Conflict.</p>
 */
public class DuplicateResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }
}
