package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL copybook CVACT01Y.cpy — the ACCOUNT-RECORD structure.
 * <p>
 * COBOL layout (300 bytes total):
 * <pre>
 *   05  ACCT-ID                  PIC 9(11).
 *   05  ACCT-ACTIVE-STATUS       PIC X(01).
 *   05  ACCT-CURR-BAL            PIC S9(10)V99.
 *   05  ACCT-CREDIT-LIMIT        PIC S9(10)V99.
 *   05  ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99.
 *   05  ACCT-OPEN-DATE           PIC X(10).
 *   05  ACCT-EXPIRAION-DATE      PIC X(10).
 *   05  ACCT-REISSUE-DATE        PIC X(10).
 *   05  ACCT-CURR-CYC-CREDIT     PIC S9(10)V99.
 *   05  ACCT-CURR-CYC-DEBIT      PIC S9(10)V99.
 *   05  ACCT-ADDR-ZIP            PIC X(10).
 *   05  ACCT-GROUP-ID            PIC X(10).
 *   05  FILLER                   PIC X(178).
 * </pre>
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

    /** Total record length in bytes as defined by the COBOL copybook. */
    public static final int RECORD_LENGTH = 300;

    /**
     * Formats the account ID as an 11-digit zero-padded string,
     * matching the COBOL PIC 9(11) display format.
     */
    public String formattedAcctId() {
        return String.format("%011d", acctId);
    }
}
