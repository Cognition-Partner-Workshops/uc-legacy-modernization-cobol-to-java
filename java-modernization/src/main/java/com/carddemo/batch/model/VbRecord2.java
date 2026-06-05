package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL variable-length record 2 (VBRC-REC2).
 *
 * <pre>
 *   05  VB2-ACCT-ID              PIC 9(11)
 *   05  VB2-ACCT-CURR-BAL        PIC S9(10)V99
 *   05  VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *   05  VB2-ACCT-REISSUE-YYYY    PIC X(04)
 * </pre>
 *
 * Record length: 39 bytes.
 */
public record VbRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {
    public static final int RECORD_LENGTH = 39;
}
