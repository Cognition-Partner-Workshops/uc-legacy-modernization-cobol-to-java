package com.carddemo.batch.model;

/**
 * Maps the short variable-length record VBRC-REC1.
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID                PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 */
public record VbRecord1(
        String acctId,
        String activeStatus
) {

    public static final int LENGTH = 12;
}
