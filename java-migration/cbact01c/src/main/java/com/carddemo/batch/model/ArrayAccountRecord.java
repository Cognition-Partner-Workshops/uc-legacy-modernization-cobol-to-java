package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps to the COBOL ARR-ARRAY-REC structure written to ARRYFILE.
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
 * Each array entry holds a balance and a cycle-debit amount.
 * The COBOL program populates only entries 1-3:
 * <ul>
 *   <li>[0]: account balance, debit=1005.00</li>
 *   <li>[1]: account balance, debit=1525.00</li>
 *   <li>[2]: balance=-1025.00, debit=-2500.00</li>
 *   <li>[3..4]: zeroed (INITIALIZE)</li>
 * </ul>
 */
public record ArrayAccountRecord(
        long acctId,
        List<BalanceEntry> entries
) {

    /** A single balance/debit pair in the array. */
    public record BalanceEntry(BigDecimal currBal, BigDecimal currCycDebit) {}

    /** Number of array slots as defined by OCCURS 5. */
    public static final int ARRAY_SIZE = 5;

    /**
     * Build an array record from an input account record, applying the
     * COBOL business rules from paragraph 1400-POPUL-ARRAY-RECORD.
     */
    public static ArrayAccountRecord fromAccountRecord(AccountRecord input) {
        BalanceEntry[] slots = new BalanceEntry[ARRAY_SIZE];
        // Slot 0: actual balance, hardcoded debit 1005.00
        slots[0] = new BalanceEntry(input.currBal(), new BigDecimal("1005.00"));
        // Slot 1: actual balance, hardcoded debit 1525.00
        slots[1] = new BalanceEntry(input.currBal(), new BigDecimal("1525.00"));
        // Slot 2: hardcoded values
        slots[2] = new BalanceEntry(new BigDecimal("-1025.00"), new BigDecimal("-2500.00"));
        // Slots 3-4: zeroed (matching INITIALIZE)
        slots[3] = new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
        slots[4] = new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);

        return new ArrayAccountRecord(input.acctId(), List.of(slots));
    }
}
