package com.cardemo.batch.model;

/**
 * Maps to COBOL VBRC-REC1 — a short variable-length record (12 bytes).
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID              PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS   PIC X(01).
 * </pre>
 */
public record VbRecord1(long acctId, String activeStatus) {

    public String toDelimitedLine() {
        return String.format("%011d|%s", acctId, activeStatus);
    }
}
