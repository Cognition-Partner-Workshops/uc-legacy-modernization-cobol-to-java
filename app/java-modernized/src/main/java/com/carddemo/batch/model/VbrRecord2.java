package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL variable-length record 2 (VBRC-REC2, 39 bytes).
 *
 * <pre>
 * 01 VBRC-REC2.
 *    05  VB2-ACCT-ID                PIC 9(11).
 *    05  VB2-ACCT-CURR-BAL          PIC S9(10)V99.
 *    05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *    05  VB2-ACCT-REISSUE-YYYY      PIC X(04).
 * </pre>
 */
public record VbrRecord2(
        String acctId,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        String acctReissueYear
) {

    public String toOutputLine() {
        return String.join("|",
                acctId,
                acctCurrBal.toPlainString(),
                acctCreditLimit.toPlainString(),
                acctReissueYear
        );
    }
}
