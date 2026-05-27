package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps VBRC-REC2 — the longer variable-length record (39 bytes in COBOL).
 *
 * <pre>
 * 01 VBRC-REC2.
 *    05  VB2-ACCT-ID                PIC 9(11).
 *    05  VB2-ACCT-CURR-BAL          PIC S9(10)V99.
 *    05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *    05  VB2-ACCT-REISSUE-YYYY      PIC X(04).
 * </pre>
 */
public record VbRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {

    public String toDelimited() {
        return String.join("|",
                String.format("%011d", acctId),
                currBal.toPlainString(),
                creditLimit.toPlainString(),
                reissueYear);
    }

    public static VbRecord2 parseDelimited(String line) {
        String[] parts = line.split("\\|", -1);
        return new VbRecord2(
                Long.parseLong(parts[0].trim()),
                new BigDecimal(parts[1].trim()),
                new BigDecimal(parts[2].trim()),
                parts[3].trim());
    }
}
