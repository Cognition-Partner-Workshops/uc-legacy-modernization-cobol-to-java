package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL VBRC-REC2 — a longer variable-length record (39 bytes).
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
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        String reissueYear
) {

    public String toDelimitedLine() {
        return String.join("|",
                String.format("%011d", acctId),
                currentBalance.toPlainString(),
                creditLimit.toPlainString(),
                reissueYear
        );
    }
}
