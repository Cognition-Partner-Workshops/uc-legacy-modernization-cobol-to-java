package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to VBRC-REC2 — longer variable-length record (39 bytes in COBOL).
 * Contains account ID, balance, credit limit, and reissue year.
 */
public record VbRecord2(
        long acctId,                    // PIC 9(11)
        BigDecimal currBal,             // PIC S9(10)V99
        BigDecimal creditLimit,         // PIC S9(10)V99
        String reissueYear              // PIC X(04)
) {
    public String toDelimitedString() {
        return String.join("|",
                String.valueOf(acctId),
                currBal.toPlainString(),
                creditLimit.toPlainString(),
                reissueYear);
    }
}
