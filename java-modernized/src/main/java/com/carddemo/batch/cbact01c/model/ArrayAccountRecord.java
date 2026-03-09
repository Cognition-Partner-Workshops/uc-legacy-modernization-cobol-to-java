package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Java equivalent of the COBOL ARR-ARRAY-REC (FD ARRY-FILE, LRECL 110).
 *
 * <pre>
 *  01 ARR-ARRAY-REC.
 *     05  ARR-ACCT-ID                PIC 9(11).
 *     05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *       10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *       10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 COMP-3.
 *     05  ARR-FILLER                 PIC X(04).
 * </pre>
 */
public record ArrayAccountRecord(
        long acctId,
        List<BalanceDebitPair> balanceDebitPairs
) {
    /** Number of array elements defined in the COBOL OCCURS clause. */
    public static final int OCCURS_COUNT = 5;

    /**
     * A single element of the OCCURS array: balance + cycle debit.
     */
    public record BalanceDebitPair(
            BigDecimal currBal,
            BigDecimal currCycDebit
    ) {
        public static final BalanceDebitPair ZERO =
                new BalanceDebitPair(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
