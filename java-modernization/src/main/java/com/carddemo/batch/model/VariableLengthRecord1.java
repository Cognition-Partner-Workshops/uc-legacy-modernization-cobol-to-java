package com.carddemo.batch.model;

/**
 * Maps to COBOL working-storage record VBRC-REC1 (12 bytes).
 * Short variable-length record containing account ID and active status.
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID                PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 */
public record VariableLengthRecord1(
        long acctId,
        String acctActiveStatus
) {

    /** Record length in the COBOL program (used for MOVE ... TO VBR-REC(1:WS-RECD-LEN)). */
    public static final int RECORD_LENGTH = 12;

    /**
     * Format this record as a pipe-delimited line.
     */
    public String toDelimitedString() {
        return String.format("%011d|%s", acctId, acctActiveStatus);
    }
}
