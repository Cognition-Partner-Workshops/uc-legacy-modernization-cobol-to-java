package com.carddemo.batch.model;

/**
 * Maps to COBOL VBRC-REC1 — short variable-length record (12 bytes).
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID                PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 */
public record VbRecord1(
        long acctId,            // PIC 9(11)   11 bytes
        String activeStatus     // PIC X(01)    1 byte
) {
    public static final int RECORD_LENGTH = 12;
}
