package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Java equivalent of COBOL copybook CVACT01Y.cpy — ACCOUNT-RECORD (300 bytes).
 *
 * <pre>
 *  01  ACCOUNT-RECORD.
 *      05  ACCT-ID                      PIC 9(11).
 *      05  ACCT-ACTIVE-STATUS           PIC X(01).
 *      05  ACCT-CURR-BAL                PIC S9(10)V99.
 *      05  ACCT-CREDIT-LIMIT            PIC S9(10)V99.
 *      05  ACCT-CASH-CREDIT-LIMIT       PIC S9(10)V99.
 *      05  ACCT-OPEN-DATE               PIC X(10).
 *      05  ACCT-EXPIRAION-DATE          PIC X(10).
 *      05  ACCT-REISSUE-DATE            PIC X(10).
 *      05  ACCT-CURR-CYC-CREDIT         PIC S9(10)V99.
 *      05  ACCT-CURR-CYC-DEBIT          PIC S9(10)V99.
 *      05  ACCT-ADDR-ZIP                PIC X(10).
 *      05  ACCT-GROUP-ID                PIC X(10).
 *      05  FILLER                       PIC X(178).
 * </pre>
 */
public record AccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String addrZip,
        String groupId
) {
    /** Total record length in the COBOL file. */
    public static final int RECORD_LENGTH = 300;
}
