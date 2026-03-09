package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL FD OUT-FILE record structure (LRECL 107, RECFM FB).
 *
 * <pre>
 * 01 OUT-ACCT-REC.
 *    05  OUT-ACCT-ID                PIC 9(11).
 *    05  OUT-ACCT-ACTIVE-STATUS     PIC X(01).
 *    05  OUT-ACCT-CURR-BAL          PIC S9(10)V99.
 *    05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *    05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99.
 *    05  OUT-ACCT-OPEN-DATE         PIC X(10).
 *    05  OUT-ACCT-EXPIRAION-DATE    PIC X(10).
 *    05  OUT-ACCT-REISSUE-DATE      PIC X(10).
 *    05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99.
 *    05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 USAGE IS COMP-3.
 *    05  OUT-ACCT-GROUP-ID          PIC X(10).
 * </pre>
 *
 * Note: In the COBOL version, OUT-ACCT-CURR-CYC-DEBIT is COMP-3 (packed decimal,
 * 7 bytes for S9(10)V99). In this Java version we store it as BigDecimal and
 * serialise it as a human-readable decimal string for portability.
 */
public record OutputAccountRecord(
        long acctId,
        String acctActiveStatus,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        BigDecimal acctCashCreditLimit,
        String acctOpenDate,
        String acctExpiraionDate,
        String acctReissueDate,
        BigDecimal acctCurrCycCredit,
        BigDecimal acctCurrCycDebit,
        String acctGroupId
) {

    /**
     * Format this record as a pipe-delimited line for the output file.
     * Each field preserves the precision of the original COBOL layout.
     */
    public String toDelimitedString() {
        return String.join("|",
                String.format("%011d", acctId),
                acctActiveStatus,
                acctCurrBal.toPlainString(),
                acctCreditLimit.toPlainString(),
                acctCashCreditLimit.toPlainString(),
                acctOpenDate,
                acctExpiraionDate,
                acctReissueDate,
                acctCurrCycCredit.toPlainString(),
                acctCurrCycDebit.toPlainString(),
                acctGroupId
        );
    }
}
