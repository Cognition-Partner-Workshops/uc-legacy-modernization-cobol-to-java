package com.carddemo.batch.exception;

/**
 * Equivalent of COBOL CEE3ABD abend with code 999.
 * Thrown when an unrecoverable I/O or processing error occurs.
 */
public class BatchAbendException extends RuntimeException {

    private final int abendCode;

    public BatchAbendException(int abendCode, String message) {
        super(message);
        this.abendCode = abendCode;
    }

    public BatchAbendException(int abendCode, String message, Throwable cause) {
        super(message, cause);
        this.abendCode = abendCode;
    }

    public int getAbendCode() {
        return abendCode;
    }
}
