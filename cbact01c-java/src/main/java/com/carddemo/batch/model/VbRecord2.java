package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL VBRC-REC2 — longer variable-length record (39 bytes).
 *
 * <pre>
 * 01 VBRC-REC2.
 *    05  VB2-ACCT-ID                PIC 9(11).
 *    05  VB2-ACCT-CURR-BAL          PIC S9(10)V99.
 *    05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *    05  VB2-ACCT-REISSUE-YYYY      PIC X(04).
 * </pre>
 */
public record VbRecord2(
        long acctId,            // PIC 9(11)        11 bytes
        BigDecimal currBal,     // PIC S9(10)V99    12 bytes (zoned)
        BigDecimal creditLimit, // PIC S9(10)V99    12 bytes (zoned)
        String reissueYear      // PIC X(04)         4 bytes
) {
    public static final int RECORD_LENGTH = 39;
}
