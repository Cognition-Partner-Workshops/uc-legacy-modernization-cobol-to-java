package com.carddemo.model;

/**
 * Maps to COBOL VBRC-REC1 (12 bytes, variable-length record type 1).
 *
 * <pre>
 *  05  VB1-ACCT-ID             PIC 9(11)
 *  05  VB1-ACCT-ACTIVE-STATUS  PIC X(01)
 * </pre>
 */
public record VariableLengthRecord1(
        long acctId,
        String acctActiveStatus
) {
    public static final int RECORD_LENGTH = 12;
}
