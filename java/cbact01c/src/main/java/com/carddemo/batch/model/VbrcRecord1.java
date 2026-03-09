package com.carddemo.batch.model;

/**
 * Maps to the COBOL variable-length record type 1 (short record, 12 bytes).
 *
 * <pre>
 *  05  VB1-ACCT-ID                PIC 9(11)
 *  05  VB1-ACCT-ACTIVE-STATUS     PIC X(01)
 * </pre>
 */
public record VbrcRecord1(
        long acctId,
        String activeStatus
) {
    public static final int RECORD_LENGTH = 12;

    public static VbrcRecord1 fromAccountRecord(AccountRecord acct) {
        return new VbrcRecord1(acct.acctId(), acct.activeStatus());
    }

    public String toFixedWidth() {
        return String.format("%011d", acctId) + activeStatus;
    }
}
