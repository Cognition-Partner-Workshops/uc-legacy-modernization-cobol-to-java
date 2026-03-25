package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Array output record written to ARRY-FILE by CBACT01C.
 *
 * Maps the COBOL FD ARRY-FILE layout:
 *   ARR-ACCT-ID                  PIC 9(11)
 *   ARR-ACCT-BAL OCCURS 5 TIMES
 *     ARR-ACCT-CURR-BAL          PIC S9(10)V99
 *     ARR-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 COMP-3
 *   ARR-FILLER                   PIC X(04)
 *
 * Each entry in the 5-element array holds a balance and a debit.
 * The COBOL program only populates entries 1-3:
 *   [1] balance = input balance,    debit = 1005.00
 *   [2] balance = input balance,    debit = 1525.00
 *   [3] balance = -1025.00,         debit = -2500.00
 *   [4] and [5] remain initialized to zero.
 */
public record ArrayRecord(
        long acctId,
        BalanceEntry[] entries
) {

    public record BalanceEntry(BigDecimal balance, BigDecimal debit) {

        public static BalanceEntry zero() {
            return new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }
}
