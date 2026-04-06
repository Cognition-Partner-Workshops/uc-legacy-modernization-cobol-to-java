package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps the CVACT01Y copybook ACCOUNT-RECORD structure (300 bytes).
 * <pre>
 *   05  ACCT-ID                   PIC 9(11)
 *   05  ACCT-ACTIVE-STATUS        PIC X(01)
 *   05  ACCT-CURR-BAL             PIC S9(10)V99
 *   05  ACCT-CREDIT-LIMIT         PIC S9(10)V99
 *   05  ACCT-CASH-CREDIT-LIMIT    PIC S9(10)V99
 *   05  ACCT-OPEN-DATE            PIC X(10)
 *   05  ACCT-EXPIRAION-DATE       PIC X(10)
 *   05  ACCT-REISSUE-DATE         PIC X(10)
 *   05  ACCT-CURR-CYC-CREDIT      PIC S9(10)V99
 *   05  ACCT-CURR-CYC-DEBIT       PIC S9(10)V99
 *   05  ACCT-ADDR-ZIP             PIC X(10)
 *   05  ACCT-GROUP-ID             PIC X(10)
 *   05  FILLER                    PIC X(178)
 * </pre>
 */
public record AccountRecord(
        String acctId,              // PIC 9(11)   – 11 chars
        String acctActiveStatus,    // PIC X(01)   –  1 char
        BigDecimal acctCurrBal,     // PIC S9(10)V99 – 12 chars zoned decimal
        BigDecimal acctCreditLimit, // PIC S9(10)V99 – 12 chars zoned decimal
        BigDecimal acctCashCreditLimit, // PIC S9(10)V99 – 12 chars zoned decimal
        String acctOpenDate,        // PIC X(10)   – 10 chars  YYYY-MM-DD
        String acctExpirationDate,  // PIC X(10)   – 10 chars  YYYY-MM-DD
        String acctReissueDate,     // PIC X(10)   – 10 chars  YYYY-MM-DD
        BigDecimal acctCurrCycCredit,  // PIC S9(10)V99 – 12 chars zoned decimal
        BigDecimal acctCurrCycDebit,   // PIC S9(10)V99 – 12 chars zoned decimal
        String acctAddrZip,         // PIC X(10)   – 10 chars
        String acctGroupId          // PIC X(10)   – 10 chars
        // FILLER PIC X(178) is ignored
) {

    /** Total record length in the fixed-width input file. */
    public static final int RECORD_LENGTH = 300;

    // Field offsets and widths within the 300-byte record
    public static final int ACCT_ID_OFF = 0,                ACCT_ID_LEN = 11;
    public static final int ACTIVE_STATUS_OFF = 11,         ACTIVE_STATUS_LEN = 1;
    public static final int CURR_BAL_OFF = 12,              CURR_BAL_LEN = 12;
    public static final int CREDIT_LIMIT_OFF = 24,          CREDIT_LIMIT_LEN = 12;
    public static final int CASH_CREDIT_LIMIT_OFF = 36,     CASH_CREDIT_LIMIT_LEN = 12;
    public static final int OPEN_DATE_OFF = 48,             OPEN_DATE_LEN = 10;
    public static final int EXPIRATION_DATE_OFF = 58,        EXPIRATION_DATE_LEN = 10;
    public static final int REISSUE_DATE_OFF = 68,          REISSUE_DATE_LEN = 10;
    public static final int CURR_CYC_CREDIT_OFF = 78,       CURR_CYC_CREDIT_LEN = 12;
    public static final int CURR_CYC_DEBIT_OFF = 90,        CURR_CYC_DEBIT_LEN = 12;
    public static final int ADDR_ZIP_OFF = 102,             ADDR_ZIP_LEN = 10;
    public static final int GROUP_ID_OFF = 112,             GROUP_ID_LEN = 10;

    /**
     * Generates a display string matching the COBOL 1100-DISPLAY-ACCT-RECORD paragraph.
     */
    public String toDisplayString() {
        return """
                ACCT-ID                 :%s
                ACCT-ACTIVE-STATUS      :%s
                ACCT-CURR-BAL           :%s
                ACCT-CREDIT-LIMIT       :%s
                ACCT-CASH-CREDIT-LIMIT  :%s
                ACCT-OPEN-DATE          :%s
                ACCT-EXPIRAION-DATE     :%s
                ACCT-REISSUE-DATE       :%s
                ACCT-CURR-CYC-CREDIT    :%s
                ACCT-CURR-CYC-DEBIT     :%s
                ACCT-GROUP-ID           :%s
                -------------------------------------------------"""
                .formatted(
                        acctId, acctActiveStatus,
                        acctCurrBal, acctCreditLimit, acctCashCreditLimit,
                        acctOpenDate, acctExpirationDate, acctReissueDate,
                        acctCurrCycCredit, acctCurrCycDebit,
                        acctGroupId
                );
    }
}
