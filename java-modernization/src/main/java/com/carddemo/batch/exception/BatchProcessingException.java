package com.carddemo.batch.exception;

/**
 * Thrown when the batch processing encounters an unrecoverable error,
 * analogous to the COBOL 9999-ABEND-PROGRAM paragraph.
 */
public class BatchProcessingException extends RuntimeException {

    private final int abendCode;

    public BatchProcessingException(String message, int abendCode) {
        super(message);
        this.abendCode = abendCode;
    }

    public BatchProcessingException(String message, int abendCode, Throwable cause) {
        super(message, cause);
        this.abendCode = abendCode;
    }

    public int getAbendCode() {
        return abendCode;
    }
}
