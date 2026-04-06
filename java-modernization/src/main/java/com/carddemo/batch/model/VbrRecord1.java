package com.carddemo.batch.model;

/**
 * Maps the COBOL VBRC-REC1 structure (variable-length record type 1, 12 bytes).
 * <pre>
 *   05  VB1-ACCT-ID              PIC 9(11)
 *   05  VB1-ACCT-ACTIVE-STATUS   PIC X(01)
 * </pre>
 */
public record VbrRecord1(
        String acctId,           // PIC 9(11)
        String acctActiveStatus  // PIC X(01)
) {
    /** Fixed length of this record type in the COBOL program. */
    public static final int RECORD_LENGTH = 12;

    public String toDelimitedLine() {
        return acctId + "|" + acctActiveStatus;
    }
}
