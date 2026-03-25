package com.carddemo.batch.model;

/**
 * Maps to COBOL VBRC-REC1 – variable-length record type 1 (12 bytes).
 * Contains account ID and active status.
 */
public record VbRecord1(
        long acctId,
        String activeStatus
) {
}
