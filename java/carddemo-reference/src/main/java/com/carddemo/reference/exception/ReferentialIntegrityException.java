package com.carddemo.reference.exception;

/**
 * Thrown when an operation would violate referential integrity, e.g. deleting
 * a {@code TransactionType} that still owns categories.
 *
 * <p>Models the DB2 {@code FOREIGN KEY ... ON DELETE RESTRICT} constraint
 * declared in {@code app/app-transaction-type-db2/ddl/TRNTYCAT.ddl} and the
 * "referential integrity checking" performed by the legacy delete path in
 * {@code COTRTLIC}. Surfaces as HTTP 409 Conflict.</p>
 */
public class ReferentialIntegrityException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ReferentialIntegrityException(String message) {
        super(message);
    }
}
