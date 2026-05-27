package com.carddemo.batch.model;

/**
 * Maps VBRC-REC1 — the short variable-length record (12 bytes in COBOL).
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID                PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS     PIC X(01).
 * </pre>
 */
public record VbRecord1(long acctId, String activeStatus) {

    public String toDelimited() {
        return String.format("%011d", acctId) + "|" + activeStatus;
    }

    public static VbRecord1 parseDelimited(String line) {
        String[] parts = line.split("\\|", -1);
        return new VbRecord1(Long.parseLong(parts[0].trim()), parts[1]);
    }
}
