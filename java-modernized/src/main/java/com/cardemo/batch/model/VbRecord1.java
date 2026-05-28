package com.cardemo.batch.model;

/**
 * Corresponds to COBOL VBRC-REC1 (short variable-length record: account ID + status).
 *
 * <pre>
 * 01 VBRC-REC1.
 *    05  VB1-ACCT-ID              PIC 9(11).
 *    05  VB1-ACCT-ACTIVE-STATUS   PIC X(01).
 * </pre>
 */
public record VbRecord1(
        long acctId,
        String activeStatus
) {

    public String toCsv() {
        return acctId + "," + activeStatus;
    }

    public static String csvHeader() {
        return "ACCT_ID,ACTIVE_STATUS";
    }
}
