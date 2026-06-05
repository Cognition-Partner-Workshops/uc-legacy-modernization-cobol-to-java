package com.carddemo.batch.model;

/**
 * Maps to COBOL variable-length record 1 (VBRC-REC1).
 *
 * <pre>
 *   05  VB1-ACCT-ID              PIC 9(11)
 *   05  VB1-ACCT-ACTIVE-STATUS   PIC X(01)
 * </pre>
 *
 * Record length: 12 bytes.
 */
public record VbRecord1(long acctId, String activeStatus) {
    public static final int RECORD_LENGTH = 12;
}
