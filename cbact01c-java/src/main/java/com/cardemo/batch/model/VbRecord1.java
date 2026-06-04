package com.cardemo.batch.model;

/**
 * Maps to VBRC-REC1 — short variable-length record (12 bytes in COBOL).
 * Contains account ID and active status.
 */
public record VbRecord1(
        long acctId,                    // PIC 9(11)
        String activeStatus             // PIC X(01)
) {
    public String toDelimitedString() {
        return acctId + "|" + activeStatus;
    }
}
