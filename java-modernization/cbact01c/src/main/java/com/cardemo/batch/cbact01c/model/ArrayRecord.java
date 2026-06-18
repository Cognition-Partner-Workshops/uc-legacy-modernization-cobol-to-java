package com.cardemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Mirrors ARR-ARRAY-REC — the array-structured output written to ARRYFILE.
 *
 * <pre>
 * 01 ARR-ARRAY-REC.
 *    05  ARR-ACCT-ID                PIC 9(11).
 *    05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *      10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *      10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99  USAGE IS COMP-3.
 *    05  ARR-FILLER                 PIC X(04).
 * </pre>
 *
 * Each of the 5 balance entries pairs a current-balance with a cycle-debit.
 * The COBOL program populates indices 1-3 and leaves 4-5 zeroed.
 */
public record ArrayRecord(
        long acctId,
        BalanceEntry[] balanceEntries,
        String filler
) {

    public static final int NUM_ENTRIES = 5;

    public record BalanceEntry(
            BigDecimal currBal,
            BigDecimal currCycDebit
    ) {
        public static final BalanceEntry ZERO =
                new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
