package com.carddemo.batch.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps to the COBOL ARR-ARRAY-REC structure written to ARRYFILE.
 * <p>
 * COBOL layout:
 * <pre>
 *   05  ARR-ACCT-ID                PIC 9(11).
 *   05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *     10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *     10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3.
 *   05  ARR-FILLER                 PIC X(04).
 * </pre>
 *
 * @param acctId         the account identifier
 * @param balanceEntries exactly 5 balance/debit pairs
 */
public record ArrayAccountRecord(
        long acctId,
        List<BalanceEntry> balanceEntries
) {

    public ArrayAccountRecord {
        if (balanceEntries.size() != 5) {
            throw new IllegalArgumentException(
                    "Expected exactly 5 balance entries, got " + balanceEntries.size());
        }
        balanceEntries = List.copyOf(balanceEntries);
    }

    /**
     * Returns a pipe-delimited string representation suitable for the array output file.
     */
    public String toOutputLine() {
        String entriesStr = balanceEntries.stream()
                .map(e -> e.currentBalance().toPlainString() + "," + e.currentCycleDebit().toPlainString())
                .collect(Collectors.joining("|"));
        return String.format("%011d", acctId) + "|" + entriesStr;
    }
}
