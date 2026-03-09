package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Variable-length record type 2 (39 bytes in COBOL).
 *
 * <pre>
 * 05  VB2-ACCT-ID              PIC 9(11)
 * 05  VB2-ACCT-CURR-BAL        PIC S9(10)V99
 * 05  VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99
 * 05  VB2-ACCT-REISSUE-YYYY    PIC X(04)
 * </pre>
 */
public record VbRecord2(
        long acctId,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        String acctReissueYear
) {}
