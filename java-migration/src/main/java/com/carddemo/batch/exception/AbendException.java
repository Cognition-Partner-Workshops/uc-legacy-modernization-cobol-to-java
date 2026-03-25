package com.carddemo.batch.exception;

/**
 * Equivalent of COBOL CEE3ABD abend.
 * Thrown when the batch program encounters an unrecoverable I/O error.
 */
public class AbendException extends RuntimeException {

    private final int abendCode;

    public AbendException(int abendCode, String message) {
        super(message);
        this.abendCode = abendCode;
    }

    public AbendException(int abendCode, String message, Throwable cause) {
        super(message, cause);
        this.abendCode = abendCode;
    }

    public int getAbendCode() {
        return abendCode;
    }
}
