package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Represents a single element in the COBOL ARR-ACCT-BAL array (OCCURS 5 TIMES).
 * <p>
 * COBOL layout:
 * <pre>
 *   10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *   10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3.
 * </pre>
 */
public record BalanceEntry(
        BigDecimal currentBalance,
        BigDecimal currentCycleDebit
) {

    /** A zero-valued balance entry, equivalent to COBOL INITIALIZE. */
    public static final BalanceEntry ZERO = new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
}
