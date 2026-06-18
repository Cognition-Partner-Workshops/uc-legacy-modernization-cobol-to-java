package com.cardemo.batch.cbact01c.model;

/**
 * Short variable-length record (12 bytes) — account id + active status.
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID                PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 */
public record VbRecord1(
        long acctId,
        char activeStatus
) {
    public static final int RECORD_LENGTH = 12;
}
