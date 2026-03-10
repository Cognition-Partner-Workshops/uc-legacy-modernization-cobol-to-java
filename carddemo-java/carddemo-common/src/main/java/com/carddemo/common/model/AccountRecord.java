package com.carddemo.common.model;

import java.math.BigDecimal;

/**
 * Maps COBOL copybook CVACT01Y - Account entity (RECLN 300).
 *
 * <pre>
 * 05  ACCT-ID                   PIC 9(11)
 * 05  ACCT-ACTIVE-STATUS        PIC X(01)
 * 05  ACCT-CURR-BAL             PIC S9(10)V99
 * 05  ACCT-CREDIT-LIMIT         PIC S9(10)V99
 * 05  ACCT-CASH-CREDIT-LIMIT    PIC S9(10)V99
 * 05  ACCT-OPEN-DATE            PIC X(10)
 * 05  ACCT-EXPIRAION-DATE       PIC X(10)
 * 05  ACCT-REISSUE-DATE         PIC X(10)
 * 05  ACCT-CURR-CYC-CREDIT      PIC S9(10)V99
 * 05  ACCT-CURR-CYC-DEBIT       PIC S9(10)V99
 * 05  ACCT-ADDR-ZIP             PIC X(10)
 * 05  ACCT-GROUP-ID             PIC X(10)
 * 05  FILLER                    PIC X(178)
 * </pre>
 */
public record AccountRecord(
        String acctId,              // PIC 9(11) - 11 chars
        String activeStatus,        // PIC X(01) - 1 char
        BigDecimal currBal,         // PIC S9(10)V99 - 12+sign chars
        BigDecimal creditLimit,     // PIC S9(10)V99
        BigDecimal cashCreditLimit, // PIC S9(10)V99
        String openDate,            // PIC X(10)
        String expirationDate,      // PIC X(10)
        String reissueDate,         // PIC X(10)
        BigDecimal currCycCredit,   // PIC S9(10)V99
        BigDecimal currCycDebit,    // PIC S9(10)V99
        String addrZip,             // PIC X(10)
        String groupId              // PIC X(10)
) {
    /** Total record length in bytes (COBOL RECLN 300). */
    public static final int RECORD_LENGTH = 300;

    /** Field widths matching the COBOL PIC definitions. */
    public static final int ACCT_ID_LEN = 11;
    public static final int ACTIVE_STATUS_LEN = 1;
    public static final int SIGNED_DECIMAL_LEN = 12; // S9(10)V99 display = 12 chars + sign
    public static final int DATE_LEN = 10;
    public static final int ZIP_LEN = 10;
    public static final int GROUP_ID_LEN = 10;
    public static final int FILLER_LEN = 178;
}
