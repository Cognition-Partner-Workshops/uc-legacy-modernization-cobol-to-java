package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps the COBOL VBRC-REC2 structure (variable-length record type 2, 39 bytes).
 * <pre>
 *   05  VB2-ACCT-ID              PIC 9(11)
 *   05  VB2-ACCT-CURR-BAL        PIC S9(10)V99
 *   05  VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *   05  VB2-ACCT-REISSUE-YYYY    PIC X(04)
 * </pre>
 */
public record VbrRecord2(
        String acctId,              // PIC 9(11)
        BigDecimal acctCurrBal,     // PIC S9(10)V99
        BigDecimal acctCreditLimit, // PIC S9(10)V99
        String acctReissueYyyy      // PIC X(04)
) {
    /** Fixed length of this record type in the COBOL program. */
    public static final int RECORD_LENGTH = 39;

    public String toDelimitedLine() {
        return String.join("|",
                acctId,
                acctCurrBal.toPlainString(),
                acctCreditLimit.toPlainString(),
                acctReissueYyyy
        );
    }
}
