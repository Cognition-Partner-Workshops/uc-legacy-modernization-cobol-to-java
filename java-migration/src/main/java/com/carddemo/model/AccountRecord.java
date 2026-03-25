package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y — the account master record (300 bytes).
 * <p>
 * COBOL layout:
 * <pre>
 *   05 ACCT-ID                  PIC 9(11)
 *   05 ACCT-ACTIVE-STATUS       PIC X(01)
 *   05 ACCT-CURR-BAL            PIC S9(10)V99
 *   05 ACCT-CREDIT-LIMIT        PIC S9(10)V99
 *   05 ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99
 *   05 ACCT-OPEN-DATE           PIC X(10)
 *   05 ACCT-EXPIRAION-DATE      PIC X(10)
 *   05 ACCT-REISSUE-DATE        PIC X(10)
 *   05 ACCT-CURR-CYC-CREDIT     PIC S9(10)V99
 *   05 ACCT-CURR-CYC-DEBIT      PIC S9(10)V99
 *   05 ACCT-ADDR-ZIP            PIC X(10)
 *   05 ACCT-GROUP-ID            PIC X(10)
 *   05 FILLER                   PIC X(178)
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

    /** Total record length in the fixed-width file. */
    public static final int RECORD_LENGTH = 300;

    // ── field positions (0-based, inclusive start, exclusive end) ──
    private static final int ACCT_ID_START = 0;
    private static final int ACCT_ID_END = 11;
    private static final int STATUS_POS = 11;
    private static final int CURR_BAL_START = 12;
    private static final int CURR_BAL_END = 24;
    private static final int CREDIT_LIMIT_START = 24;
    private static final int CREDIT_LIMIT_END = 36;
    private static final int CASH_CREDIT_START = 36;
    private static final int CASH_CREDIT_END = 48;
    private static final int OPEN_DATE_START = 48;
    private static final int OPEN_DATE_END = 58;
    private static final int EXPIRY_DATE_START = 58;
    private static final int EXPIRY_DATE_END = 68;
    private static final int REISSUE_DATE_START = 68;
    private static final int REISSUE_DATE_END = 78;
    private static final int CYC_CREDIT_START = 78;
    private static final int CYC_CREDIT_END = 90;
    private static final int CYC_DEBIT_START = 90;
    private static final int CYC_DEBIT_END = 102;
    private static final int ADDR_ZIP_START = 102;
    private static final int ADDR_ZIP_END = 112;
    private static final int GROUP_ID_START = 112;
    private static final int GROUP_ID_END = 122;

    /**
     * Parse a 300-byte fixed-width line into an {@link AccountRecord}.
     * Handles COBOL zoned-decimal sign encoding where the last byte
     * encodes both a digit and the sign (e.g. '{' = +0, '}' = -0).
     */
    public static AccountRecord parse(String line) {
        if (line.length() < GROUP_ID_END) {
            throw new IllegalArgumentException(
                    "Account record too short: " + line.length() + " chars (need >= " + GROUP_ID_END + ")");
        }

        long acctId = Long.parseLong(line.substring(ACCT_ID_START, ACCT_ID_END));
        String status = line.substring(STATUS_POS, STATUS_POS + 1);
        BigDecimal currBal = parseSignedDecimal(line.substring(CURR_BAL_START, CURR_BAL_END), 2);
        BigDecimal creditLimit = parseSignedDecimal(line.substring(CREDIT_LIMIT_START, CREDIT_LIMIT_END), 2);
        BigDecimal cashCredit = parseSignedDecimal(line.substring(CASH_CREDIT_START, CASH_CREDIT_END), 2);
        String openDate = line.substring(OPEN_DATE_START, OPEN_DATE_END);
        String expiryDate = line.substring(EXPIRY_DATE_START, EXPIRY_DATE_END);
        String reissueDate = line.substring(REISSUE_DATE_START, REISSUE_DATE_END);
        BigDecimal cycCredit = parseSignedDecimal(line.substring(CYC_CREDIT_START, CYC_CREDIT_END), 2);
        BigDecimal cycDebit = parseSignedDecimal(line.substring(CYC_DEBIT_START, CYC_DEBIT_END), 2);
        String addrZip = line.substring(ADDR_ZIP_START, ADDR_ZIP_END).trim();
        String groupId = line.substring(GROUP_ID_START, GROUP_ID_END);

        return new AccountRecord(
                acctId, status, currBal, creditLimit, cashCredit,
                openDate, expiryDate, reissueDate,
                cycCredit, cycDebit, addrZip, groupId);
    }

    /**
     * Decode a COBOL zoned-decimal field (ASCII representation).
     * The last character may be a sign-overpunch:
     * <ul>
     *   <li>{@code {} = +0, A-I = +1 to +9</li>
     *   <li>{@code }} = -0, J-R = -1 to -9</li>
     * </ul>
     *
     * @param raw   the raw field string
     * @param scale number of implied decimal places (V99 → 2)
     */
    public static BigDecimal parseSignedDecimal(String raw, int scale) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        int lastDigit;
        boolean negative;

        switch (lastChar) {
            case '{' -> { lastDigit = 0; negative = false; }
            case 'A' -> { lastDigit = 1; negative = false; }
            case 'B' -> { lastDigit = 2; negative = false; }
            case 'C' -> { lastDigit = 3; negative = false; }
            case 'D' -> { lastDigit = 4; negative = false; }
            case 'E' -> { lastDigit = 5; negative = false; }
            case 'F' -> { lastDigit = 6; negative = false; }
            case 'G' -> { lastDigit = 7; negative = false; }
            case 'H' -> { lastDigit = 8; negative = false; }
            case 'I' -> { lastDigit = 9; negative = false; }
            case '}' -> { lastDigit = 0; negative = true; }
            case 'J' -> { lastDigit = 1; negative = true; }
            case 'K' -> { lastDigit = 2; negative = true; }
            case 'L' -> { lastDigit = 3; negative = true; }
            case 'M' -> { lastDigit = 4; negative = true; }
            case 'N' -> { lastDigit = 5; negative = true; }
            case 'O' -> { lastDigit = 6; negative = true; }
            case 'P' -> { lastDigit = 7; negative = true; }
            case 'Q' -> { lastDigit = 8; negative = true; }
            case 'R' -> { lastDigit = 9; negative = true; }
            default -> {
                // Plain digit — no overpunch
                lastDigit = Character.getNumericValue(lastChar);
                negative = false;
            }
        }

        String numStr = digits + lastDigit;
        BigDecimal value = new BigDecimal(numStr).movePointLeft(scale);
        return negative ? value.negate() : value;
    }
}
