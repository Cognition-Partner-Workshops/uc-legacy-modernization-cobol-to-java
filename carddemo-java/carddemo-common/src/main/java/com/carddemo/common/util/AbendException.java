package com.carddemo.common.util;

/**
 * Replaces the COBOL CEE3ABD abend call. Thrown when a batch program
 * encounters an unrecoverable error (e.g., file I/O failure).
 */
public class AbendException extends RuntimeException {

    private final int abendCode;

    public AbendException(int abendCode, String message) {
        super("ABEND " + abendCode + ": " + message);
        this.abendCode = abendCode;
    }

    public AbendException(int abendCode, String message, Throwable cause) {
        super("ABEND " + abendCode + ": " + message, cause);
        this.abendCode = abendCode;
    }

    public int getAbendCode() {
        return abendCode;
    }
}
