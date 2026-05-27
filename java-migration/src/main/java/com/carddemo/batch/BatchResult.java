package com.carddemo.batch;

/**
 * Captures the outcome of a batch run — mirrors the COBOL APPL-RESULT / ABCODE semantics.
 */
public record BatchResult(int recordsProcessed, int returnCode, String message) {

    public static final int RC_OK    = 0;
    public static final int RC_EOF   = 16;
    public static final int RC_ERROR = 12;
    public static final int RC_ABEND = 999;

    public boolean isSuccess() {
        return returnCode == RC_OK || returnCode == RC_EOF;
    }
}
