package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps the COBOL ARR-ARRAY-REC written to the array output file.
 *
 * <pre>
 * 01 ARR-ARRAY-REC.
 *    05  ARR-ACCT-ID                PIC 9(11).
 *    05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *      10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *      10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99  USAGE IS COMP-3.
 *    05  ARR-FILLER                 PIC X(04).
 * </pre>
 */
public record ArrayAccountRecord(
        String acctId,
        List<BalanceEntry> balanceEntries,
        String filler
) {

    public static final int NUM_ENTRIES = 5;

    public record BalanceEntry(
            BigDecimal currBal,
            BigDecimal currCycDebit
    ) {
    }
}
