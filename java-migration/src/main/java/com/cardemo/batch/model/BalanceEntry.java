package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * One element of the OCCURS 5 TIMES array inside ARR-ARRAY-REC.
 *
 * <pre>
 * 05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *   10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *   10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3.
 * </pre>
 */
public record BalanceEntry(
        BigDecimal currBal,
        BigDecimal currCycDebit
) {
    public static final BalanceEntry ZERO = new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
}
