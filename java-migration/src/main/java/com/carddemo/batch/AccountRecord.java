package com.carddemo.batch;

import java.math.BigDecimal;

/**
 * Domain model corresponding to COBOL copybook CVACT01Y (ACCOUNT-RECORD).
 * Record length: 300 bytes (fixed).
 *
 * <pre>
 * 01  ACCOUNT-RECORD.
 *     05  ACCT-ID                   PIC 9(11).
 *     05  ACCT-ACTIVE-STATUS        PIC X(01).
 *     05  ACCT-CURR-BAL             PIC S9(10)V99.
 *     05  ACCT-CREDIT-LIMIT         PIC S9(10)V99.
 *     05  ACCT-CASH-CREDIT-LIMIT    PIC S9(10)V99.
 *     05  ACCT-OPEN-DATE            PIC X(10).
 *     05  ACCT-EXPIRAION-DATE       PIC X(10).
 *     05  ACCT-REISSUE-DATE         PIC X(10).
 *     05  ACCT-CURR-CYC-CREDIT      PIC S9(10)V99.
 *     05  ACCT-CURR-CYC-DEBIT       PIC S9(10)V99.
 *     05  ACCT-ADDR-ZIP             PIC X(10).
 *     05  ACCT-GROUP-ID             PIC X(10).
 *     05  FILLER                    PIC X(178).
 * </pre>
 */
public record AccountRecord(
        long acctId,                    // PIC 9(11)
        String acctActiveStatus,        // PIC X(01)
        BigDecimal acctCurrBal,         // PIC S9(10)V99
        BigDecimal acctCreditLimit,     // PIC S9(10)V99
        BigDecimal acctCashCreditLimit, // PIC S9(10)V99
        String acctOpenDate,            // PIC X(10)
        String acctExpirationDate,      // PIC X(10)  (note: COBOL has typo "EXPIRAION")
        String acctReissueDate,         // PIC X(10)
        BigDecimal acctCurrCycCredit,   // PIC S9(10)V99
        BigDecimal acctCurrCycDebit,    // PIC S9(10)V99
        String acctAddrZip,             // PIC X(10)
        String acctGroupId              // PIC X(10)
) {
    public static final int RECORD_LENGTH = 300;
}
