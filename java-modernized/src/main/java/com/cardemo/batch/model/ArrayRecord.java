package com.cardemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Corresponds to COBOL FD ARRY-FILE record (ARR-ARRAY-REC).
 *
 * <pre>
 * 01 ARR-ARRAY-REC.
 *    05  ARR-ACCT-ID              PIC 9(11).
 *    05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *      10  ARR-ACCT-CURR-BAL      PIC S9(10)V99.
 *      10  ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99 COMP-3.
 *    05  ARR-FILLER               PIC X(04).
 * </pre>
 */
public record ArrayRecord(
        long acctId,
        List<BalanceEntry> balanceEntries
) {

    public static final int NUM_BALANCE_SLOTS = 5;

    public record BalanceEntry(BigDecimal currBal, BigDecimal currCycDebit) {

        public String toCsv() {
            return currBal.toPlainString() + "," + currCycDebit.toPlainString();
        }
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append(acctId);
        for (BalanceEntry entry : balanceEntries) {
            sb.append(",").append(entry.toCsv());
        }
        return sb.toString();
    }

    public static String csvHeader() {
        StringBuilder sb = new StringBuilder("ACCT_ID");
        for (int i = 1; i <= NUM_BALANCE_SLOTS; i++) {
            sb.append(",BAL_").append(i).append("_CURR_BAL");
            sb.append(",BAL_").append(i).append("_CYC_DEBIT");
        }
        return sb.toString();
    }
}
