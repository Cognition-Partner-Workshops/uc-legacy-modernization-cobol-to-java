package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL VBRC-REC2 variable-length record (type 2, 39 bytes).
 * <p>
 * COBOL layout:
 * <pre>
 *   05  VB2-ACCT-ID                PIC 9(11).
 *   05  VB2-ACCT-CURR-BAL          PIC S9(10)V99.
 *   05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *   05  VB2-ACCT-REISSUE-YYYY      PIC X(04).
 * </pre>
 *
 * @param acctId       the account identifier
 * @param currentBalance  the current account balance
 * @param creditLimit     the account credit limit
 * @param reissueYear     the 4-digit reissue year
 */
public record VbrcRecord2(
        long acctId,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        String reissueYear
) {

    /**
     * Returns a pipe-delimited string representation.
     */
    public String toOutputLine() {
        return String.join("|",
                String.format("%011d", acctId),
                currentBalance.toPlainString(),
                creditLimit.toPlainString(),
                reissueYear
        );
    }
}
