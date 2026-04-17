package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of VBRC-REC2 — the longer (39-byte) variable-length record.
 * <pre>
 *  05  VB2-ACCT-ID              PIC 9(11)
 *  05  VB2-ACCT-CURR-BAL        PIC S9(10)V99
 *  05  VB2-ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *  05  VB2-ACCT-REISSUE-YYYY    PIC X(04)
 * </pre>
 */
public record VbrcRecord2(
        long acctId,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        String acctReissueYyyy
) {

    public static final int RECORD_LENGTH = 39;

    public String toFixedWidth() {
        return String.format("%011d", acctId)
                + OutputAccountRecord.formatSignedZoned(acctCurrBal)
                + OutputAccountRecord.formatSignedZoned(acctCreditLimit)
                + String.format("%-4s", acctReissueYyyy);
    }
}
