package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Maps to the COBOL FD ARRY-FILE record layout.
 *
 * <pre>
 *  05  ARR-ACCT-ID                PIC 9(11)
 *  05  ARR-ACCT-BAL OCCURS 5 TIMES
 *    10  ARR-ACCT-CURR-BAL        PIC S9(10)V99
 *    10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99  USAGE IS COMP-3
 *  05  ARR-FILLER                 PIC X(04)
 * </pre>
 *
 * Each of the 5 array entries holds a balance and a cycle-debit amount.
 */
public record ArrayRecord(
        long acctId,
        BigDecimal[] balances,
        BigDecimal[] debits
) {

    /** Number of array slots in the COBOL OCCURS clause. */
    public static final int ARRAY_SIZE = 5;

    /** Hard-coded debit values from the COBOL source. */
    private static final BigDecimal DEBIT_SLOT_1 = new BigDecimal("1005.00");
    private static final BigDecimal DEBIT_SLOT_2 = new BigDecimal("1525.00");
    private static final BigDecimal BAL_SLOT_3 = new BigDecimal("-1025.00");
    private static final BigDecimal DEBIT_SLOT_3 = new BigDecimal("-2500.00");

    /**
     * Populate the array record from the source account, replicating
     * the logic of COBOL paragraph {@code 1400-POPUL-ARRAY-RECORD}.
     */
    public static ArrayRecord fromAccount(AccountRecord acct) {
        BigDecimal[] bals = new BigDecimal[ARRAY_SIZE];
        BigDecimal[] debts = new BigDecimal[ARRAY_SIZE];

        // COBOL INITIALIZE zeroes all numeric fields
        Arrays.fill(bals, BigDecimal.ZERO);
        Arrays.fill(debts, BigDecimal.ZERO);

        // Slot 1
        bals[0] = acct.acctCurrBal();
        debts[0] = DEBIT_SLOT_1;

        // Slot 2
        bals[1] = acct.acctCurrBal();
        debts[1] = DEBIT_SLOT_2;

        // Slot 3
        bals[2] = BAL_SLOT_3;
        debts[2] = DEBIT_SLOT_3;

        // Slots 4 & 5 remain zero (COBOL INITIALIZE)

        return new ArrayRecord(acct.acctId(), bals, debts);
    }

    /**
     * Serialize to a pipe-delimited line for the array output file.
     */
    public String toDelimitedLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        for (int i = 0; i < ARRAY_SIZE; i++) {
            sb.append('|').append(balances[i].toPlainString());
            sb.append('|').append(debits[i].toPlainString());
        }
        return sb.toString();
    }
}
