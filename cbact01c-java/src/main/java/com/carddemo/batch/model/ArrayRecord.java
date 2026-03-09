package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL FD ARRY-FILE / ARR-ARRAY-REC (LRECL=110 from JCL).
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
 * Each OCCURS entry: 12 bytes (zoned) + 7 bytes (COMP-3) = 19 bytes.
 * Total: 11 + (5 * 19) + 4 = 11 + 95 + 4 = 110 bytes (matches JCL LRECL).
 */
public record ArrayRecord(
        long acctId,
        ArrayEntry[] entries   // exactly 5 elements
) {

    public static final int OCCURS_COUNT = 5;
    public static final int RECORD_LENGTH = 110;

    /**
     * One occurrence of the balance / debit pair within the array record.
     */
    public record ArrayEntry(
            BigDecimal currBal,       // PIC S9(10)V99  (zoned decimal, 12 bytes)
            BigDecimal currCycDebit   // PIC S9(10)V99  COMP-3 (packed decimal, 7 bytes)
    ) {
        public static final ArrayEntry ZERO = new ArrayEntry(BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
