package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Corresponds to the COBOL FD OUT-FILE record (LRECL 107).
 *
 * <pre>
 * 05  OUT-ACCT-ID                PIC 9(11)
 * 05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 * 05  OUT-ACCT-CURR-BAL          PIC S9(10)V99
 * 05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 * 05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 * 05  OUT-ACCT-OPEN-DATE         PIC X(10)
 * 05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 * 05  OUT-ACCT-REISSUE-DATE      PIC X(10)
 * 05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 * 05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 COMP-3  (7 bytes)
 * 05  OUT-ACCT-GROUP-ID          PIC X(10)
 * </pre>
 */
public record OutAccountRecord(
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
     * Default debit value applied when the source ACCT-CURR-CYC-DEBIT is zero.
     * Mirrors COBOL: {@code MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT}.
     */
    public static final BigDecimal DEFAULT_DEBIT = new BigDecimal("2525.00");
}
