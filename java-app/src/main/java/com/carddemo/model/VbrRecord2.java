package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL VBRC-REC2 (39 bytes): ACCT-ID + CURR-BAL + CREDIT-LIMIT + REISSUE-YYYY.
 */
public record VbrRecord2(
        long acctId,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        String reissueYear
) {

    public String toOutputLine() {
        return String.format("%011d", acctId)
                + formatDecimal(currentBalance)
                + formatDecimal(creditLimit)
                + String.format("%-4s", reissueYear);
    }

    private static String formatDecimal(BigDecimal val) {
        boolean negative = val.signum() < 0;
        BigDecimal abs = val.abs();
        long unscaled = abs.movePointRight(2).longValue();
        String digits = String.format("%012d", unscaled);
        return (negative ? "-" : "+") + digits;
    }
}
