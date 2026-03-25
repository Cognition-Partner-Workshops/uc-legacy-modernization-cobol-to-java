package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Output record written to OUT-FILE by CBACT01C.
 *
 * Maps the COBOL FD OUT-FILE layout:
 *   OUT-ACCT-ID                PIC 9(11)
 *   OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 *   OUT-ACCT-CURR-BAL          PIC S9(10)V99
 *   OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *   OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *   OUT-ACCT-OPEN-DATE         PIC X(10)
 *   OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 *   OUT-ACCT-REISSUE-DATE      PIC X(10)  (reformatted to YYYYMMDD)
 *   OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *   OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 COMP-3
 *   OUT-ACCT-GROUP-ID          PIC X(10)
 *
 * Note: CURR-CYC-DEBIT is COMP-3 (packed decimal) in the output,
 * and if the input debit is zero, the COBOL program substitutes 2525.00.
 */
public record OutputAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currentCycleCredit,
        BigDecimal currentCycleDebit,
        String groupId
) {
}
