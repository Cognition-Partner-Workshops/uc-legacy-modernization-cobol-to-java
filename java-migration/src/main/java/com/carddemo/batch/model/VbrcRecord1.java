package com.carddemo.batch.model;

/**
 * Java equivalent of VBRC-REC1 in CBACT01C (short variable-length record).
 *
 * COBOL layout (12 bytes):
 *   05  VB1-ACCT-ID              PIC 9(11)
 *   05  VB1-ACCT-ACTIVE-STATUS   PIC X(01)
 */
public record VbrcRecord1(
        long acctId,
        String activeStatus
) {

    public String toDelimitedLine() {
        return String.format("%011d", acctId) + "|" + activeStatus;
    }

    public static VbrcRecord1 fromDelimitedLine(String line) {
        String[] parts = line.split("\\|", -1);
        return new VbrcRecord1(Long.parseLong(parts[0]), parts[1]);
    }
}
