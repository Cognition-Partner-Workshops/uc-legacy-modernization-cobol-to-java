package com.cardemo.batch.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Maps to the COBOL ARR-ARRAY-REC — an array-based record with 5 balance/debit
 * slots written to the array output file.
 *
 * <pre>
 * FD ARRY-FILE.
 * 01 ARR-ARRAY-REC.
 *    05  ARR-ACCT-ID                PIC 9(11).
 *    05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *      10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *      10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3.
 *    05  ARR-FILLER                 PIC X(04).
 * </pre>
 */
public record ArrayRecord(
        long acctId,
        BalanceSlot[] slots
) {

    public record BalanceSlot(BigDecimal balance, BigDecimal cycleDebit) {

        @Override
        public String toString() {
            return balance.toPlainString() + "," + cycleDebit.toPlainString();
        }
    }

    public static final int SLOT_COUNT = 5;

    public String toDelimitedLine() {
        String slotData = Arrays.stream(slots)
                .map(BalanceSlot::toString)
                .collect(Collectors.joining("|"));
        return String.format("%011d|%s", acctId, slotData);
    }
}
