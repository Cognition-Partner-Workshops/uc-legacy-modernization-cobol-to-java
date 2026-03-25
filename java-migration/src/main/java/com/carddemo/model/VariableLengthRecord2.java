package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL VBRC-REC2 (39 bytes, variable-length record type 2).
 *
 * <pre>
 *  05  VB2-ACCT-ID             PIC 9(11)
 *  05  VB2-ACCT-CURR-BAL       PIC S9(10)V99
 *  05  VB2-ACCT-CREDIT-LIMIT   PIC S9(10)V99
 *  05  VB2-ACCT-REISSUE-YYYY   PIC X(04)
 * </pre>
 */
public record VariableLengthRecord2(
        long acctId,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        String acctReissueYear
) {
    public static final int RECORD_LENGTH = 39;
}
