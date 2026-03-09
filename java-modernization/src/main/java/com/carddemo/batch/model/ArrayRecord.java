package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Maps to the COBOL FD ARRY-FILE record structure (LRECL 110, RECFM FB).
 *
 * <pre>
 * 01 ARR-ARRAY-REC.
 *    05  ARR-ACCT-ID                PIC 9(11).
 *    05  ARR-ACCT-BAL OCCURS 5 TIMES.
 *      10  ARR-ACCT-CURR-BAL        PIC S9(10)V99.
 *      10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3.
 *    05  ARR-FILLER                 PIC X(04).
 * </pre>
 *
 * Each of the 5 balance slots holds a current-balance and a cycle-debit pair.
 * In the COBOL program, only slots 1-3 are populated; slots 4-5 remain zero.
 */
public record ArrayRecord(
        long acctId,
        BigDecimal[] balances,
        BigDecimal[] cycleDebits
) {

    /** Number of balance/debit slots in the array. */
    public static final int SLOT_COUNT = 5;

    public ArrayRecord {
        if (balances.length != SLOT_COUNT) {
            throw new IllegalArgumentException("balances must have " + SLOT_COUNT + " elements");
        }
        if (cycleDebits.length != SLOT_COUNT) {
            throw new IllegalArgumentException("cycleDebits must have " + SLOT_COUNT + " elements");
        }
        // Defensive copy
        balances = Arrays.copyOf(balances, SLOT_COUNT);
        cycleDebits = Arrays.copyOf(cycleDebits, SLOT_COUNT);
    }

    /**
     * Create a new zero-initialised array record (mirrors COBOL INITIALIZE).
     */
    public static ArrayRecord initialise(long acctId) {
        BigDecimal[] bals = new BigDecimal[SLOT_COUNT];
        BigDecimal[] debits = new BigDecimal[SLOT_COUNT];
        Arrays.fill(bals, BigDecimal.ZERO);
        Arrays.fill(debits, BigDecimal.ZERO);
        return new ArrayRecord(acctId, bals, debits);
    }

    /**
     * Format this record as a pipe-delimited line for the output file.
     */
    public String toDelimitedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        for (int i = 0; i < SLOT_COUNT; i++) {
            sb.append('|').append(balances[i].toPlainString());
            sb.append('|').append(cycleDebits[i].toPlainString());
        }
        return sb.toString();
    }
}
