package com.carddemo.batch.cbact01c.model;

/**
 * Maps to the short variable-length record written by COBOL paragraph
 * {@code 1550-WRITE-VB1-RECORD} (12 bytes: ACCT-ID + ACTIVE-STATUS).
 *
 * <pre>
 *  05  VB1-ACCT-ID                PIC 9(11)
 *  05  VB1-ACCT-ACTIVE-STATUS     PIC X(01)
 * </pre>
 */
public record VbRecord1(
        long acctId,
        String acctActiveStatus
) {

    public static VbRecord1 fromAccount(AccountRecord acct) {
        return new VbRecord1(acct.acctId(), acct.acctActiveStatus());
    }

    public String toDelimitedLine() {
        return String.format("%011d", acctId) + "|" + acctActiveStatus;
    }
}
