package com.carddemo.batch.cbact01c.model;

/**
 * Java equivalent of the COBOL VBRC-REC1 (variable-length record, 12 bytes).
 *
 * <pre>
 *  01 VBRC-REC1.
 *     05  VB1-ACCT-ID                PIC 9(11).
 *     05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 */
public record VbrRecord1(
        long acctId,
        String activeStatus
) {
}
