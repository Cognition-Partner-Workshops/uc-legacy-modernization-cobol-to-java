package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps to the COBOL FD ARRY-FILE record (ARR-ARRAY-REC).
 * <p>
 * Contains an account ID and an array of 5 balance/debit pairs.
 * CBACT01C populates only entries 1–3:
 * <ul>
 *   <li>Entry 1: actual balance, debit = 1005.00</li>
 *   <li>Entry 2: actual balance, debit = 1525.00</li>
 *   <li>Entry 3: balance = −1025.00, debit = −2500.00</li>
 *   <li>Entries 4–5: zeroes (INITIALIZE)</li>
 * </ul>
 */
public record ArrayAccountRecord(
        long acctId,
        List<BalanceEntry> entries
) {

    /** One element of the OCCURS 5 TIMES array. */
    public record BalanceEntry(
            BigDecimal currBal,
            BigDecimal currCycDebit
    ) {
        public static final BalanceEntry ZERO = new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
