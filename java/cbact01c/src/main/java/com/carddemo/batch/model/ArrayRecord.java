package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Corresponds to the COBOL FD ARRY-FILE record (LRECL=110 from JCL).
 *
 * <pre>
 *   05  ARR-ACCT-ID                PIC 9(11)
 *   05  ARR-ACCT-BAL OCCURS 5 TIMES
 *     10  ARR-ACCT-CURR-BAL        PIC S9(10)V99       (12 bytes display)
 *     10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 COMP-3 (7 bytes)
 *   05  ARR-FILLER                 PIC X(04)
 * </pre>
 *
 * Total = 11 + 5*(12+7) + 4 = 11 + 95 + 4 = 110 bytes
 */
public record ArrayRecord(
        long acctId,
        List<BalanceEntry> balanceEntries,
        String filler
) {
    /** One element of the OCCURS 5 TIMES array. */
    public record BalanceEntry(
            BigDecimal currBal,
            BigDecimal currCycDebit
    ) {
        public static final BalanceEntry ZERO = new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
