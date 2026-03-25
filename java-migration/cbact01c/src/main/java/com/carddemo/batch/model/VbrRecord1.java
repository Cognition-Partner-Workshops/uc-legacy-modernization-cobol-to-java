package com.carddemo.batch.model;

/**
 * Variable-length record type 1 — short record (12 bytes in COBOL).
 *
 * COBOL equivalent:
 * <pre>
 *   01 VBRC-REC1.
 *      05  VB1-ACCT-ID                PIC 9(11).
 *      05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 */
public record VbrRecord1(
        long acctId,
        String activeStatus
) {
}
