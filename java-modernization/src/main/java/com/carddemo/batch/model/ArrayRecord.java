package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Java equivalent of the ARR-ARRAY-REC file-descriptor in CBACT01C.
 * <p>
 * COBOL layout:
 * <pre>
 *  05  ARR-ACCT-ID                PIC 9(11)
 *  05  ARR-ACCT-BAL OCCURS 5 TIMES
 *    10  ARR-ACCT-CURR-BAL        PIC S9(10)V99
 *    10  ARR-ACCT-CURR-CYC-DEBIT  PIC S9(10)V99 USAGE IS COMP-3
 *  05  ARR-FILLER                 PIC X(04)
 * </pre>
 * Each OCCURS slot has a zoned-decimal balance and a COMP-3 debit.
 */
public record ArrayRecord(
        long acctId,
        BigDecimal[] balances,
        BigDecimal[] debits
) {
    public static final int SLOT_COUNT = 5;

    public ArrayRecord {
        if (balances.length != SLOT_COUNT || debits.length != SLOT_COUNT) {
            throw new IllegalArgumentException("Array record must have exactly " + SLOT_COUNT + " slots");
        }
    }

    /**
     * Create an initialized (zeroed) array record for the given account.
     */
    public static ArrayRecord initialized(long acctId) {
        BigDecimal[] balances = new BigDecimal[SLOT_COUNT];
        BigDecimal[] debits = new BigDecimal[SLOT_COUNT];
        Arrays.fill(balances, BigDecimal.ZERO);
        Arrays.fill(debits, BigDecimal.ZERO);
        return new ArrayRecord(acctId, balances, debits);
    }

    /**
     * Format as a fixed-width text line mirroring the COBOL WRITE output.
     */
    public String toFixedWidth() {
        var sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        for (int i = 0; i < SLOT_COUNT; i++) {
            sb.append(OutputAccountRecord.formatSignedZoned(balances[i]));
            sb.append(OutputAccountRecord.formatComp3Display(debits[i]));
        }
        sb.append("    "); // ARR-FILLER PIC X(04)
        return sb.toString();
    }
}
