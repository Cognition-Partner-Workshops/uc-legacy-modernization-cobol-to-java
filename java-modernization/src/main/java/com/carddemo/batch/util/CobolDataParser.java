package com.carddemo.batch.util;

import com.carddemo.batch.model.AccountRecord;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses fixed-width COBOL data lines into Java domain objects.
 * <p>
 * Handles COBOL PIC S9(n)V99 DISPLAY format with trailing overpunch sign
 * encoding (ASCII convention).
 */
public final class CobolDataParser {

    private CobolDataParser() { /* utility */ }

    // ---- field positions (0-based) matching CVACT01Y.cpy layout (300 bytes) ----
    private static final int ACCT_ID_START              = 0;
    private static final int ACCT_ID_END                = 11;
    private static final int ACTIVE_STATUS_START        = 11;
    private static final int ACTIVE_STATUS_END          = 12;
    private static final int CURR_BAL_START             = 12;
    private static final int CURR_BAL_END               = 24;
    private static final int CREDIT_LIMIT_START         = 24;
    private static final int CREDIT_LIMIT_END           = 36;
    private static final int CASH_CREDIT_LIMIT_START    = 36;
    private static final int CASH_CREDIT_LIMIT_END      = 48;
    private static final int OPEN_DATE_START            = 48;
    private static final int OPEN_DATE_END              = 58;
    private static final int EXPIRAION_DATE_START       = 58;
    private static final int EXPIRAION_DATE_END         = 68;
    private static final int REISSUE_DATE_START         = 68;
    private static final int REISSUE_DATE_END           = 78;
    private static final int CURR_CYC_CREDIT_START      = 78;
    private static final int CURR_CYC_CREDIT_END        = 90;
    private static final int CURR_CYC_DEBIT_START       = 90;
    private static final int CURR_CYC_DEBIT_END         = 102;
    private static final int ADDR_ZIP_START             = 102;
    private static final int ADDR_ZIP_END               = 112;
    private static final int GROUP_ID_START             = 112;
    private static final int GROUP_ID_END               = 122;

    /**
     * Parses a 300-byte fixed-width line into an {@link AccountRecord}.
     *
     * @param line the raw fixed-width text (right-padded with spaces to 300 chars)
     * @return a fully populated AccountRecord
     * @throws IllegalArgumentException if the line is shorter than the required fields
     */
    public static AccountRecord parseAccountRecord(String line) {
        // Pad to full record length if shorter (trailing spaces)
        String padded = padRight(line, AccountRecord.RECORD_LENGTH);

        long acctId = Long.parseLong(padded.substring(ACCT_ID_START, ACCT_ID_END).trim());
        String activeStatus = padded.substring(ACTIVE_STATUS_START, ACTIVE_STATUS_END);
        BigDecimal currBal = parseSignedDecimal(padded.substring(CURR_BAL_START, CURR_BAL_END), 2);
        BigDecimal creditLimit = parseSignedDecimal(padded.substring(CREDIT_LIMIT_START, CREDIT_LIMIT_END), 2);
        BigDecimal cashCreditLimit = parseSignedDecimal(padded.substring(CASH_CREDIT_LIMIT_START, CASH_CREDIT_LIMIT_END), 2);
        String openDate = padded.substring(OPEN_DATE_START, OPEN_DATE_END);
        String expiraionDate = padded.substring(EXPIRAION_DATE_START, EXPIRAION_DATE_END);
        String reissueDate = padded.substring(REISSUE_DATE_START, REISSUE_DATE_END);
        BigDecimal currCycCredit = parseSignedDecimal(padded.substring(CURR_CYC_CREDIT_START, CURR_CYC_CREDIT_END), 2);
        BigDecimal currCycDebit = parseSignedDecimal(padded.substring(CURR_CYC_DEBIT_START, CURR_CYC_DEBIT_END), 2);
        String addrZip = padded.substring(ADDR_ZIP_START, ADDR_ZIP_END).trim();
        String groupId = padded.substring(GROUP_ID_START, GROUP_ID_END).trim();

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expiraionDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    /**
     * Parses a COBOL DISPLAY signed numeric field with trailing overpunch.
     * <p>
     * ASCII overpunch convention:
     * <ul>
     *   <li>Positive: {@code 0→{, 1→A, 2→B, 3→C, 4→D, 5→E, 6→F, 7→G, 8→H, 9→I}</li>
     *   <li>Negative: {@code 0→}, 1→J, 2→K, 3→L, 4→M, 5→N, 6→O, 7→P, 8→Q, 9→R}</li>
     * </ul>
     *
     * @param raw           the raw COBOL display field (e.g. "00000001940{")
     * @param decimalPlaces number of implied decimal places (V99 → 2)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parseSignedDecimal(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        int sign = 1;
        char lastDigit;

        // Decode overpunch
        if (lastChar >= '0' && lastChar <= '9') {
            // No overpunch — treat as unsigned positive
            lastDigit = lastChar;
        } else if (lastChar == '{') {
            lastDigit = '0';
        } else if (lastChar == '}') {
            lastDigit = '0';
            sign = -1;
        } else if (lastChar >= 'A' && lastChar <= 'I') {
            lastDigit = (char) ('1' + (lastChar - 'A'));
        } else if (lastChar >= 'J' && lastChar <= 'R') {
            lastDigit = (char) ('1' + (lastChar - 'J'));
            sign = -1;
        } else {
            throw new IllegalArgumentException(
                    "Unexpected overpunch character: '" + lastChar + "' in field: " + raw);
        }

        String digits = raw.substring(0, raw.length() - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits);
        value = value.movePointLeft(decimalPlaces);

        return sign < 0 ? value.negate() : value;
    }

    private static String padRight(String s, int length) {
        if (s.length() >= length) {
            return s;
        }
        return s + " ".repeat(length - s.length());
    }
}
