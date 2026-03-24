package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL copybook CVACT01Y.cpy — ACCOUNT-RECORD (300-byte record).
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
        String acctId,               // PIC 9(11)
        String acctActiveStatus,     // PIC X(01)
        BigDecimal acctCurrBal,      // PIC S9(10)V99
        BigDecimal acctCreditLimit,  // PIC S9(10)V99
        BigDecimal acctCashCreditLimit, // PIC S9(10)V99
        String acctOpenDate,         // PIC X(10)  YYYY-MM-DD
        String acctExpiraionDate,    // PIC X(10)  (typo preserved from COBOL)
        String acctReissueDate,      // PIC X(10)  YYYY-MM-DD
        BigDecimal acctCurrCycCredit,// PIC S9(10)V99
        BigDecimal acctCurrCycDebit, // PIC S9(10)V99
        String acctAddrZip,          // PIC X(10)
        String acctGroupId           // PIC X(10)
) {

    /** Total record length in the flat file. */
    public static final int RECORD_LENGTH = 300;
}
