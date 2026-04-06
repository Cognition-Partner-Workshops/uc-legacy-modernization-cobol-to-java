package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maps the COBOL ARR-ARRAY-REC file-descriptor structure written to ARRYFILE.
 * <pre>
 *   05  ARR-ACCT-ID                PIC 9(11)
 *   05  ARR-ACCT-BAL OCCURS 5 TIMES
 *     10  ARR-ACCT-CURR-BAL        PIC S9(10)V99
 *     10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3
 *   05  ARR-FILLER                 PIC X(04)
 * </pre>
 *
 * @param acctId    the 11-digit account identifier
 * @param balances  exactly 5 balance-slot entries (some may be zero-filled)
 */
public record ArrayAccountRecord(
        String acctId,
        List<BalanceSlot> balances
) {
    /** A single occurrence of the OCCURS 5 TIMES array element. */
    public record BalanceSlot(BigDecimal currBal, BigDecimal currCycDebit) {

        /** A zeroed-out slot (matches COBOL INITIALIZE behaviour). */
        public static final BalanceSlot ZERO = new BalanceSlot(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /** Number of array slots defined in the COBOL OCCURS clause. */
    public static final int SLOT_COUNT = 5;

    /**
     * Serialises to a pipe-delimited text line for the array output file.
     */
    public String toDelimitedLine() {
        var sb = new StringBuilder(acctId);
        for (int i = 0; i < SLOT_COUNT; i++) {
            BalanceSlot slot = (i < balances.size()) ? balances.get(i) : BalanceSlot.ZERO;
            sb.append('|').append(slot.currBal().toPlainString());
            sb.append('|').append(slot.currCycDebit().toPlainString());
        }
        return sb.toString();
    }
}
