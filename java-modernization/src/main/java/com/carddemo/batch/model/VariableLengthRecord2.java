package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL working-storage record VBRC-REC2 (39 bytes).
 * Longer variable-length record with balance info and reissue year.
 *
 * <pre>
 * 01 VBRC-REC2.
 *    05  VB2-ACCT-ID                PIC 9(11).
 *    05  VB2-ACCT-CURR-BAL          PIC S9(10)V99.
 *    05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *    05  VB2-ACCT-REISSUE-YYYY      PIC X(04).
 * </pre>
 */
public record VariableLengthRecord2(
        long acctId,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        String acctReissueYear
) {

    /** Record length in the COBOL program. */
    public static final int RECORD_LENGTH = 39;

    /**
     * Format this record as a pipe-delimited line.
     */
    public String toDelimitedString() {
        return String.join("|",
                String.format("%011d", acctId),
                acctCurrBal.toPlainString(),
                acctCreditLimit.toPlainString(),
                acctReissueYear
        );
    }
}
