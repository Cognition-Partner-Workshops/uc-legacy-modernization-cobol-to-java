package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Domain model for the COBOL ACCOUNT-RECORD (copybook CVACT01Y).
 *
 * Maps the 300-byte fixed-width record layout:
 *   ACCT-ID              PIC 9(11)
 *   ACCT-ACTIVE-STATUS   PIC X(01)
 *   ACCT-CURR-BAL        PIC S9(10)V99
 *   ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *   ACCT-OPEN-DATE       PIC X(10)
 *   ACCT-EXPIRAION-DATE  PIC X(10)   (sic - original COBOL typo)
 *   ACCT-REISSUE-DATE    PIC X(10)
 *   ACCT-CURR-CYC-CREDIT PIC S9(10)V99
 *   ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
 *   ACCT-ADDR-ZIP        PIC X(10)
 *   ACCT-GROUP-ID        PIC X(10)
 *   FILLER               PIC X(178)
 */
public record AccountRecord(
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
        String addressZip,
        String groupId
) {

    /** Total record length in bytes. */
    public static final int RECORD_LENGTH = 300;
}
