package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Corresponds to COBOL VBRC-REC2 (longer variable-length record: financial summary).
 *
 * <pre>
 * 01 VBRC-REC2.
 *    05  VB2-ACCT-ID              PIC 9(11).
 *    05  VB2-ACCT-CURR-BAL        PIC S9(10)V99.
 *    05  VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99.
 *    05  VB2-ACCT-REISSUE-YYYY    PIC X(04).
 * </pre>
 */
public record VbRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {

    public String toCsv() {
        return String.join(",",
                String.valueOf(acctId),
                currBal.toPlainString(),
                creditLimit.toPlainString(),
                reissueYear
        );
    }

    public static String csvHeader() {
        return "ACCT_ID,CURR_BAL,CREDIT_LIMIT,REISSUE_YYYY";
    }
}
