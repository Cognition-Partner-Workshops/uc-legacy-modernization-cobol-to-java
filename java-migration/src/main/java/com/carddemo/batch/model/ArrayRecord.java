package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Java equivalent of the ARR-ARRAY-REC file descriptor in CBACT01C.
 *
 * COBOL layout:
 *   05  ARR-ACCT-ID                PIC 9(11)
 *   05  ARR-ACCT-BAL OCCURS 5 TIMES
 *     10  ARR-ACCT-CURR-BAL        PIC S9(10)V99
 *     10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3
 *   05  ARR-FILLER                 PIC X(04)
 *
 * Each of the 5 array entries holds a balance/debit pair.
 * The COBOL program only populates entries 1-3; entries 4-5 remain zero (from INITIALIZE).
 */
public record ArrayRecord(
        long acctId,
        List<BalanceEntry> entries
) {

    /** Number of array slots matching COBOL OCCURS 5 TIMES */
    public static final int ARRAY_SIZE = 5;

    /** Defensive copy — the record stores an unmodifiable list. */
    public ArrayRecord(long acctId, List<BalanceEntry> entries) {
        this.acctId = acctId;
        this.entries = List.copyOf(entries);
    }

    public record BalanceEntry(BigDecimal currBal, BigDecimal currCycDebit) {

        public static BalanceEntry zero() {
            return new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        public String toDelimited() {
            return currBal.toPlainString() + "," + currCycDebit.toPlainString();
        }

        public static BalanceEntry fromDelimited(String s) {
            String[] parts = s.split(",", 2);
            return new BalanceEntry(new BigDecimal(parts[0]), new BigDecimal(parts[1]));
        }
    }

    /**
     * Create a new ArrayRecord from the given populated entries, padding with
     * zeros to ARRAY_SIZE (matching COBOL INITIALIZE for unfilled slots).
     */
    public static ArrayRecord of(long acctId, BalanceEntry... populatedEntries) {
        List<BalanceEntry> list = new ArrayList<>(ARRAY_SIZE);
        Collections.addAll(list, populatedEntries);
        while (list.size() < ARRAY_SIZE) {
            list.add(BalanceEntry.zero());
        }
        return new ArrayRecord(acctId, list);
    }

    /**
     * Format as a pipe-delimited line for the output file.
     */
    public String toDelimitedLine() {
        String entriesStr = entries.stream()
                .map(BalanceEntry::toDelimited)
                .collect(Collectors.joining("|"));
        return String.format("%011d", acctId) + "|" + entriesStr;
    }

    /**
     * Parse from a pipe-delimited line.
     */
    public static ArrayRecord fromDelimitedLine(String line) {
        String[] parts = line.split("\\|", -1);
        long id = Long.parseLong(parts[0]);
        List<BalanceEntry> entries = new ArrayList<>(ARRAY_SIZE);
        for (int i = 0; i < ARRAY_SIZE; i++) {
            entries.add(BalanceEntry.fromDelimited(parts[i + 1]));
        }
        return new ArrayRecord(id, entries);
    }
}
