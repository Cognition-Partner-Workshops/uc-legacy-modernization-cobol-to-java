package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps the ARR-ARRAY-REC file descriptor — the array-based output record.
 *
 * <pre>
 * 01 ARR-ARRAY-REC.
 *    05  ARR-ACCT-ID                PIC 9(11).
 *    05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *      10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *      10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 COMP-3.
 *    05  ARR-FILLER                 PIC X(04).
 * </pre>
 */
public record ArrayRecord(
        long acctId,
        List<BalanceEntry> balanceEntries
) {

    public static final int ENTRY_COUNT = 5;

    public record BalanceEntry(BigDecimal currBal, BigDecimal currCycDebit) {}

    public String toDelimited() {
        String entries = balanceEntries.stream()
                .map(e -> e.currBal().toPlainString() + "," + e.currCycDebit().toPlainString())
                .collect(Collectors.joining("|"));
        return String.format("%011d", acctId) + "|" + entries;
    }

    public static ArrayRecord parseDelimited(String line) {
        String[] parts = line.split("\\|", -1);
        long id = Long.parseLong(parts[0].trim());
        List<BalanceEntry> entries = new java.util.ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            String[] pair = parts[i].split(",", -1);
            entries.add(new BalanceEntry(
                    new BigDecimal(pair[0].trim()),
                    new BigDecimal(pair[1].trim())));
        }
        return new ArrayRecord(id, entries);
    }
}
