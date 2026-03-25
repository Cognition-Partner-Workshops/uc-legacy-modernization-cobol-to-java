package com.carddemo.model;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * Maps to the COBOL FD ARR-ARRAY-REC — the array-based output record
 * written to ARRYFILE.
 * <p>
 * COBOL layout:
 * <pre>
 *   05 ARR-ACCT-ID               PIC 9(11)
 *   05 ARR-ACCT-BAL OCCURS 5 TIMES
 *      10 ARR-ACCT-CURR-BAL      PIC S9(10)V99
 *      10 ARR-ACCT-CURR-CYC-DEBIT PIC S9(10)V99  USAGE IS COMP-3
 *   05 ARR-FILLER                PIC X(04)
 * </pre>
 * Each of the 5 array slots holds a (balance, debit) pair.
 */
public record ArrayRecord(
        long acctId,
        BigDecimal[] balances,
        BigDecimal[] debits
) {

    /** Number of array slots in the COBOL OCCURS clause. */
    public static final int SLOT_COUNT = 5;

    public ArrayRecord {
        if (balances.length != SLOT_COUNT || debits.length != SLOT_COUNT) {
            throw new IllegalArgumentException("Array slots must have exactly " + SLOT_COUNT + " entries");
        }
    }

    /**
     * Create an empty (zeroed) array record for the given account.
     * Mirrors COBOL {@code INITIALIZE ARR-ARRAY-REC}.
     */
    public static ArrayRecord initialized(long acctId) {
        BigDecimal[] bals = new BigDecimal[SLOT_COUNT];
        BigDecimal[] debs = new BigDecimal[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            bals[i] = BigDecimal.ZERO;
            debs[i] = BigDecimal.ZERO;
        }
        return new ArrayRecord(acctId, bals, debs);
    }

    /** Format as a pipe-delimited line for the array output file. */
    public String toOutputLine() {
        StringJoiner sj = new StringJoiner("|");
        sj.add(String.format("%011d", acctId));
        for (int i = 0; i < SLOT_COUNT; i++) {
            sj.add(balances[i].toPlainString());
            sj.add(debits[i].toPlainString());
        }
        return sj.toString();
    }
}
