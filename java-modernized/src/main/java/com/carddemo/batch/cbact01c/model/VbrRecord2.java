package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Java equivalent of the COBOL VBRC-REC2 (variable-length record, 39 bytes).
 *
 * <pre>
 *  01 VBRC-REC2.
 *     05  VB2-ACCT-ID                PIC 9(11).
 *     05  VB2-ACCT-CURR-BAL          PIC S9(10)V99.
 *     05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *     05  VB2-ACCT-REISSUE-YYYY      PIC X(04).
 * </pre>
 */
public record VbrRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {
}
