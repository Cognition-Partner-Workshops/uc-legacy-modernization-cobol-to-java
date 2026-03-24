package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps to the COBOL FD ARRY-FILE record (ARR-ARRAY-REC).
 *
 * <pre>
 * FD ARRY-FILE.
 * 01 ARR-ARRAY-REC.
 *    05  ARR-ACCT-ID                PIC 9(11).
 *    05  ARR-ACCT-BAL OCCURS 5  TIMES.
 *      10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *      10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3.
 *    05  ARR-FILLER                 PIC X(04).
 * </pre>
 *
 * Each occurrence in the OCCURS 5 array holds a balance and a debit value.
 */
public record ArrayRecord(
        String acctId,
        List<BalanceEntry> balanceEntries
) {

    public record BalanceEntry(
            BigDecimal acctCurrBal,
            BigDecimal acctCurrCycDebit
    ) {
        public String toOutputSegment() {
            return acctCurrBal.toPlainString() + "|" + acctCurrCycDebit.toPlainString();
        }
    }

    /**
     * Formats this record as a pipe-delimited line for the array output file.
     */
    public String toOutputLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(acctId);
        for (BalanceEntry entry : balanceEntries) {
            sb.append("|").append(entry.toOutputSegment());
        }
        return sb.toString();
    }
}
