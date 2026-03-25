package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Java equivalent of the COBOL FD ARR-ARRAY-REC (array-format output).
 * <p>
 * COBOL layout:
 * <pre>
 *   05  ARR-ACCT-ID              PIC 9(11)
 *   05  ARR-ACCT-BAL OCCURS 5 TIMES
 *     10  ARR-ACCT-CURR-BAL      PIC S9(10)V99
 *     10  ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3
 *   05  ARR-FILLER               PIC X(04)
 * </pre>
 * <p>
 * Only slots 1-3 are populated; slots 4-5 remain at initial value (zero).
 */
public record ArrayRecord(
        long acctId,
        BalanceEntry[] balanceEntries   // OCCURS 5 TIMES
) {
    public static final int OCCURS_COUNT = 5;

    public record BalanceEntry(
            BigDecimal currBal,         // ARR-ACCT-CURR-BAL
            BigDecimal currCycDebit     // ARR-ACCT-CURR-CYC-DEBIT (COMP-3)
    ) {
        public static final BalanceEntry ZERO = new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
