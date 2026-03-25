package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Variable-length record type 2 — longer record (39 bytes in COBOL).
 *
 * COBOL equivalent:
 * <pre>
 *   01 VBRC-REC2.
 *      05  VB2-ACCT-ID                PIC 9(11).
 *      05  VB2-ACCT-CURR-BAL          PIC S9(10)V99.
 *      05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *      05  VB2-ACCT-REISSUE-YYYY      PIC X(04).
 * </pre>
 */
public record VbrRecord2(
        long acctId,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        String reissueYear
) {
}
