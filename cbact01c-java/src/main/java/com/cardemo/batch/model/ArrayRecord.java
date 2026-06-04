package com.cardemo.batch.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps to ARR-ARRAY-REC in the COBOL FD for ARRY-FILE.
 * Contains account ID plus an array of 5 balance/debit pairs (OCCURS 5 TIMES).
 */
public record ArrayRecord(
        long acctId,                    // PIC 9(11)
        List<BalanceEntry> entries      // OCCURS 5 TIMES
) {
    public static final int ARRAY_SIZE = 5;

    public record BalanceEntry(
            BigDecimal currBal,          // PIC S9(10)V99
            BigDecimal currCycDebit      // PIC S9(10)V99 COMP-3
    ) {
        public static final BalanceEntry ZERO = new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public String toDelimitedString() {
        String entriesStr = entries.stream()
                .map(e -> e.currBal().toPlainString() + "," + e.currCycDebit().toPlainString())
                .collect(Collectors.joining("|"));
        return acctId + "|" + entriesStr;
    }
}
