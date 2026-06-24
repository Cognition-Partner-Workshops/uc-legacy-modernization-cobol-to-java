package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL ARR-ARRAY-REC structure written to ARRYFILE.
 * Contains an account ID and 5 balance/debit slots.
 */
public record ArrayAccountRecord(
        long acctId,
        BigDecimal[] balances,
        BigDecimal[] debits
) {

    public static final int SLOT_COUNT = 5;

    public String toOutputLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        for (int i = 0; i < SLOT_COUNT; i++) {
            sb.append(formatDecimal(balances[i]));
            sb.append(formatDecimal(debits[i]));
        }
        sb.append("    "); // ARR-FILLER X(04)
        return sb.toString();
    }

    private static String formatDecimal(BigDecimal val) {
        boolean negative = val.signum() < 0;
        BigDecimal abs = val.abs();
        long unscaled = abs.movePointRight(2).longValue();
        String digits = String.format("%012d", unscaled);
        return (negative ? "-" : "+") + digits;
    }
}
