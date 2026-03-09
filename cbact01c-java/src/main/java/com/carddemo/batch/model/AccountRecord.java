package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y - ACCOUNT-RECORD (300 bytes).
 *
 * <pre>
 * 01  ACCOUNT-RECORD.
 *     05  ACCT-ID                  PIC 9(11).
 *     05  ACCT-ACTIVE-STATUS       PIC X(01).
 *     05  ACCT-CURR-BAL            PIC S9(10)V99.
 *     05  ACCT-CREDIT-LIMIT        PIC S9(10)V99.
 *     05  ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99.
 *     05  ACCT-OPEN-DATE           PIC X(10).
 *     05  ACCT-EXPIRAION-DATE      PIC X(10).
 *     05  ACCT-REISSUE-DATE        PIC X(10).
 *     05  ACCT-CURR-CYC-CREDIT     PIC S9(10)V99.
 *     05  ACCT-CURR-CYC-DEBIT      PIC S9(10)V99.
 *     05  ACCT-ADDR-ZIP            PIC X(10).
 *     05  ACCT-GROUP-ID            PIC X(10).
 *     05  FILLER                   PIC X(178).
 * </pre>
 */
public record AccountRecord(
        long acctId,                    // PIC 9(11)
        String activeStatus,            // PIC X(01)
        BigDecimal currBal,             // PIC S9(10)V99
        BigDecimal creditLimit,         // PIC S9(10)V99
        BigDecimal cashCreditLimit,     // PIC S9(10)V99
        String openDate,                // PIC X(10)
        String expirationDate,          // PIC X(10)   (note: COBOL has typo "EXPIRAION")
        String reissueDate,             // PIC X(10)
        BigDecimal currCycCredit,       // PIC S9(10)V99
        BigDecimal currCycDebit,        // PIC S9(10)V99
        String addrZip,                 // PIC X(10)
        String groupId                  // PIC X(10)
) {

    /** Total record length as defined by the COBOL copybook. */
    public static final int RECORD_LENGTH = 300;

    /** Length of each signed decimal field PIC S9(10)V99 in zoned-decimal display form. */
    public static final int SIGNED_DECIMAL_LEN = 12;
}
