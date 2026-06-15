package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps the longer variable-length record VBRC-REC2.
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
        String acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {

    public static final int LENGTH = 39;
}
