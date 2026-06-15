package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL FD OUT-FILE record — flat sequential output with selected account fields.
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
 * 05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 USAGE IS COMP-3
 * 05  OUT-ACCT-GROUP-ID          PIC X(10)
 * </pre>
 */
public record OutputAccountRecord(
        String acctId,
        String acctActiveStatus,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        BigDecimal acctCashCreditLimit,
        String acctOpenDate,
        String acctExpirationDate,
        String acctReissueDate,
        BigDecimal acctCurrCycCredit,
        BigDecimal acctCurrCycDebit,
        String acctGroupId
) {}
