package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Corresponds to the COBOL FD ARRY-FILE record (LRECL 110).
 *
 * <pre>
 * 05  ARR-ACCT-ID               PIC 9(11)
 * 05  ARR-ACCT-BAL OCCURS 5 TIMES
 *   10  ARR-ACCT-CURR-BAL       PIC S9(10)V99
 *   10  ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3
 * 05  ARR-FILLER                PIC X(04)
 * </pre>
 *
 * Only indices 1-3 are populated; indices 4-5 remain at zero (COBOL INITIALIZE).
 */
public record ArrayAccountRecord(
        long acctId,
        List<BalanceEntry> balanceEntries
) {

    /** Number of array occurrences in the COBOL record. */
    public static final int OCCURS_COUNT = 5;

    /** A single balance / debit pair in the array. */
    public record BalanceEntry(BigDecimal currBal, BigDecimal currCycDebit) {

        public static final BalanceEntry ZERO =
                new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /** Hardcoded debit values that the COBOL program writes into positions 1-3. */
    public static final BigDecimal DEBIT_1 = new BigDecimal("1005.00");
    public static final BigDecimal DEBIT_2 = new BigDecimal("1525.00");
    public static final BigDecimal BAL_3   = new BigDecimal("-1025.00");
    public static final BigDecimal DEBIT_3 = new BigDecimal("-2500.00");
}
