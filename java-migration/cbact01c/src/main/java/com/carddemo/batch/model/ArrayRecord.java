package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Array output record — corresponds to the ARRY-FILE FD in CBACT01C.
 *
 * COBOL equivalent:
 * <pre>
 *   FD ARRY-FILE.
 *   01 ARR-ARRAY-REC.
 *      05  ARR-ACCT-ID                PIC 9(11).
 *      05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *        10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *        10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3.
 *      05  ARR-FILLER                 PIC X(04).
 * </pre>
 *
 * The COBOL program populates only indices 1-3 (0-2 in Java),
 * leaving indices 4-5 (3-4) at their initialized zero values.
 */
public record ArrayRecord(
        long acctId,
        List<BalanceEntry> balanceEntries
) {
    /**
     * One occurrence of the ARR-ACCT-BAL array element.
     */
    public record BalanceEntry(
            BigDecimal currentBalance,
            BigDecimal currentCycleDebit
    ) {
        public static final BalanceEntry ZERO =
                new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
