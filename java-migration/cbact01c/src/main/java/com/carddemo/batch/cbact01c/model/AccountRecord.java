package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVACT01Y (ACCOUNT-RECORD).
 * <p>
 * COBOL record layout (300 bytes):
 * <pre>
 *   05  ACCT-ID                  PIC 9(11)
 *   05  ACCT-ACTIVE-STATUS       PIC X(01)
 *   05  ACCT-CURR-BAL            PIC S9(10)V99
 *   05  ACCT-CREDIT-LIMIT        PIC S9(10)V99
 *   05  ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99
 *   05  ACCT-OPEN-DATE           PIC X(10)
 *   05  ACCT-EXPIRAION-DATE      PIC X(10)
 *   05  ACCT-REISSUE-DATE        PIC X(10)
 *   05  ACCT-CURR-CYC-CREDIT     PIC S9(10)V99
 *   05  ACCT-CURR-CYC-DEBIT      PIC S9(10)V99
 *   05  ACCT-ADDR-ZIP            PIC X(10)
 *   05  ACCT-GROUP-ID            PIC X(10)
 *   05  FILLER                   PIC X(178)
 * </pre>
 */
public record AccountRecord(
        long acctId,                    // PIC 9(11)
        String activeStatus,            // PIC X(01)
        BigDecimal currBal,             // PIC S9(10)V99
        BigDecimal creditLimit,         // PIC S9(10)V99
        BigDecimal cashCreditLimit,     // PIC S9(10)V99
        String openDate,                // PIC X(10) -- YYYY-MM-DD
        String expirationDate,          // PIC X(10) -- YYYY-MM-DD
        String reissueDate,             // PIC X(10) -- YYYY-MM-DD
        BigDecimal currCycCredit,       // PIC S9(10)V99
        BigDecimal currCycDebit,        // PIC S9(10)V99
        String addrZip,                 // PIC X(10)
        String groupId                  // PIC X(10)
) {
    /** COBOL record length including FILLER */
    public static final int RECORD_LENGTH = 300;
}
