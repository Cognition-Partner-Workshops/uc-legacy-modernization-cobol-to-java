package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y.cpy — ACCOUNT-RECORD (300-byte VSAM KSDS record).
 *
 * <pre>
 * 05  ACCT-ID                  PIC 9(11)
 * 05  ACCT-ACTIVE-STATUS       PIC X(01)
 * 05  ACCT-CURR-BAL            PIC S9(10)V99
 * 05  ACCT-CREDIT-LIMIT        PIC S9(10)V99
 * 05  ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99
 * 05  ACCT-OPEN-DATE           PIC X(10)
 * 05  ACCT-EXPIRAION-DATE      PIC X(10)
 * 05  ACCT-REISSUE-DATE        PIC X(10)
 * 05  ACCT-CURR-CYC-CREDIT     PIC S9(10)V99
 * 05  ACCT-CURR-CYC-DEBIT      PIC S9(10)V99
 * 05  ACCT-ADDR-ZIP            PIC X(10)
 * 05  ACCT-GROUP-ID            PIC X(10)
 * 05  FILLER                   PIC X(178)
 * </pre>
 */
public record AccountRecord(
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
        String acctAddrZip,
        String acctGroupId
) {
    public static final int RECORD_LENGTH = 300;
}
