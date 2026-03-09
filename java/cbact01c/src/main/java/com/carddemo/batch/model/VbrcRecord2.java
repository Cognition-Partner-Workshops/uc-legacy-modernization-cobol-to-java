package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Corresponds to COBOL VBRC-REC2 — longer variable-length record (39 bytes).
 *
 * <pre>
 *   05  VB2-ACCT-ID                PIC 9(11)
 *   05  VB2-ACCT-CURR-BAL          PIC S9(10)V99   (12 bytes display)
 *   05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99   (12 bytes display)
 *   05  VB2-ACCT-REISSUE-YYYY      PIC X(04)
 * </pre>
 *
 * Total = 11 + 12 + 12 + 4 = 39 bytes
 */
public record VbrcRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYyyy
) {
    public static final int RECORD_LENGTH = 39;
}
