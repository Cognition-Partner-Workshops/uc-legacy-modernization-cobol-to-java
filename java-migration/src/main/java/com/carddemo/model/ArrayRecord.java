package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL FD ARRY-FILE record — account ID with a 5-element balance/debit array.
 *
 * <pre>
 * 05  ARR-ACCT-ID               PIC 9(11)
 * 05  ARR-ACCT-BAL OCCURS 5 TIMES
 *   10  ARR-ACCT-CURR-BAL       PIC S9(10)V99
 *   10  ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 USAGE IS COMP-3
 * 05  ARR-FILLER                PIC X(04)
 * </pre>
 */
public record ArrayRecord(
        String acctId,
        ArrayEntry[] entries
) {
    public static final int ENTRY_COUNT = 5;

    public record ArrayEntry(
            BigDecimal acctCurrBal,
            BigDecimal acctCurrCycDebit
    ) {
        public static final ArrayEntry ZERO = new ArrayEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
