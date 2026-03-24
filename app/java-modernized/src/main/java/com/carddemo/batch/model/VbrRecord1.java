package com.carddemo.batch.model;

/**
 * Maps to the COBOL variable-length record 1 (VBRC-REC1, 12 bytes).
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID                PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 */
public record VbrRecord1(
        String acctId,
        String acctActiveStatus
) {

    public String toOutputLine() {
        return acctId + "|" + acctActiveStatus;
    }
}
