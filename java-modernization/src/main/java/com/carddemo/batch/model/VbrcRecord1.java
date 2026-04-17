package com.carddemo.batch.model;

/**
 * Java equivalent of VBRC-REC1 — the short (12-byte) variable-length record.
 * <pre>
 *  05  VB1-ACCT-ID              PIC 9(11)
 *  05  VB1-ACCT-ACTIVE-STATUS   PIC X(01)
 * </pre>
 */
public record VbrcRecord1(
        long acctId,
        String acctActiveStatus
) {

    public static final int RECORD_LENGTH = 12;

    public String toFixedWidth() {
        return String.format("%011d", acctId) + acctActiveStatus;
    }
}
