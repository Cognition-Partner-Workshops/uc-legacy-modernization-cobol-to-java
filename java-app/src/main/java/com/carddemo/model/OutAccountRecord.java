package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL OUT-ACCT-REC structure written to OUTFILE.
 */
public record OutAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currentCycleCredit,
        BigDecimal currentCycleDebit,
        String groupId
) {

    public String toOutputLine() {
        return String.format("%011d", acctId)
                + activeStatus
                + formatDecimal(currentBalance)
                + formatDecimal(creditLimit)
                + formatDecimal(cashCreditLimit)
                + String.format("%-10s", openDate)
                + String.format("%-10s", expirationDate)
                + String.format("%-10s", reissueDate)
                + formatDecimal(currentCycleCredit)
                + formatDecimal(currentCycleDebit)
                + String.format("%-10s", groupId);
    }

    private static String formatDecimal(BigDecimal val) {
        boolean negative = val.signum() < 0;
        BigDecimal abs = val.abs();
        long unscaled = abs.movePointRight(2).longValue();
        String digits = String.format("%012d", unscaled);
        return (negative ? "-" : "+") + digits;
    }
}
