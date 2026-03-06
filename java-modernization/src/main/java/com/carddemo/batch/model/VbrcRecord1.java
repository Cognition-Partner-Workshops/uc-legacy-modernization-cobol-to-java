package com.carddemo.batch.model;

/**
 * Maps to the COBOL VBRC-REC1 variable-length record (type 1, 12 bytes).
 * <p>
 * COBOL layout:
 * <pre>
 *   05  VB1-ACCT-ID                PIC 9(11).
 *   05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 *
 * @param acctId       the account identifier
 * @param activeStatus the account active status flag
 */
public record VbrcRecord1(
        long acctId,
        String activeStatus
) {

    /**
     * Returns a pipe-delimited string representation.
     */
    public String toOutputLine() {
        return String.format("%011d", acctId) + "|" + activeStatus;
    }
}
