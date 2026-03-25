package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Maps to the long variable-length record written by COBOL paragraph
 * {@code 1575-WRITE-VB2-RECORD} (39 bytes: ACCT-ID + BAL + LIMIT + YEAR).
 *
 * <pre>
 *  05  VB2-ACCT-ID                PIC 9(11)
 *  05  VB2-ACCT-CURR-BAL          PIC S9(10)V99
 *  05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *  05  VB2-ACCT-REISSUE-YYYY      PIC X(04)
 * </pre>
 */
public record VbRecord2(
        long acctId,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        String acctReissueYear
) {

    /**
     * Build from account record.  The reissue year is extracted from the
     * YYYY-MM-DD reissue date (first 4 characters).
     */
    public static VbRecord2 fromAccount(AccountRecord acct) {
        String year = acct.acctReissueDate().substring(0, 4);
        return new VbRecord2(
                acct.acctId(),
                acct.acctCurrBal(),
                acct.acctCreditLimit(),
                year
        );
    }

    public String toDelimitedLine() {
        return String.join("|",
                String.format("%011d", acctId),
                acctCurrBal.toPlainString(),
                acctCreditLimit.toPlainString(),
                acctReissueYear
        );
    }
}
