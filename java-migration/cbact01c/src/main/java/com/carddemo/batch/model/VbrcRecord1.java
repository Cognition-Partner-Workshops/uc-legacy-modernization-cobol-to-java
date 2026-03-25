package com.carddemo.batch.model;

/**
 * Maps to the COBOL VBRC-REC1 structure (first variable-length record).
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID                PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 *
 * Written with WS-RECD-LEN = 12 bytes.
 */
public record VbrcRecord1(
        long acctId,
        String activeStatus
) {

    /** Record length as set by MOVE 12 TO WS-RECD-LEN. */
    public static final int RECORD_LENGTH = 12;

    public static VbrcRecord1 fromAccountRecord(AccountRecord input) {
        return new VbrcRecord1(input.acctId(), input.activeStatus());
    }
}
