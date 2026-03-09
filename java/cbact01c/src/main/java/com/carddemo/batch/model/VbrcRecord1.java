package com.carddemo.batch.model;

/**
 * Corresponds to COBOL VBRC-REC1 — short variable-length record (12 bytes).
 *
 * <pre>
 *   05  VB1-ACCT-ID                PIC 9(11)
 *   05  VB1-ACCT-ACTIVE-STATUS     PIC X(01)
 * </pre>
 */
public record VbrcRecord1(
        long acctId,
        String activeStatus
) {
    public static final int RECORD_LENGTH = 12;
}
