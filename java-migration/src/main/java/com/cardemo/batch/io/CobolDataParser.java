package com.cardemo.batch.io;

import com.cardemo.batch.model.AccountRecord;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses fixed-width COBOL data lines into Java domain objects.
 * Handles COBOL sign-overpunch encoding for signed numeric DISPLAY fields.
 */
public final class CobolDataParser {

    private CobolDataParser() {}

    /**
     * Parse a 300-char fixed-width line into an {@link AccountRecord}.
     * Field positions correspond to the CVACT01Y copybook layout.
     */
    public static AccountRecord parseAccountRecord(String line) {
        if (line.length() < AccountRecord.RECORD_LENGTH) {
            line = padRight(line, AccountRecord.RECORD_LENGTH);
        }

        int pos = 0;
        long acctId         = Long.parseLong(line.substring(pos, pos += 11).trim());
        char activeStatus   = line.charAt(pos++);
        BigDecimal currBal       = parseSignedDecimal(line.substring(pos, pos += 12), 2);
        BigDecimal creditLimit   = parseSignedDecimal(line.substring(pos, pos += 12), 2);
        BigDecimal cashCreditLim = parseSignedDecimal(line.substring(pos, pos += 12), 2);
        String openDate     = line.substring(pos, pos += 10).trim();
        String expireDate   = line.substring(pos, pos += 10).trim();
        String reissueDate  = line.substring(pos, pos += 10).trim();
        BigDecimal cycCredit     = parseSignedDecimal(line.substring(pos, pos += 12), 2);
        BigDecimal cycDebit      = parseSignedDecimal(line.substring(pos, pos += 12), 2);
        String addrZip      = line.substring(pos, pos += 10).trim();
        String groupId      = line.substring(pos, pos + 10).trim();

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLim,
                openDate, expireDate, reissueDate, cycCredit, cycDebit,
                addrZip, groupId
        );
    }

    /**
     * Decode a COBOL signed DISPLAY numeric field using overpunch encoding.
     * The sign is embedded in the last character:
     * <ul>
     *   <li>{@code {, A-I} → positive 0-9</li>
     *   <li>{@code }, J-R} → negative 0-9</li>
     * </ul>
     *
     * @param raw          the raw fixed-width string
     * @param decimalPlaces implied decimal places (V99 → 2)
     */
    public static BigDecimal parseSignedDecimal(String raw, int decimalPlaces) {
        if (raw.isBlank()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        int sign = 1;
        int lastDigit;

        if (lastChar >= '0' && lastChar <= '9') {
            lastDigit = lastChar - '0';
        } else if (lastChar == '{') {
            lastDigit = 0;
        } else if (lastChar == '}') {
            lastDigit = 0;
            sign = -1;
        } else if (lastChar >= 'A' && lastChar <= 'I') {
            lastDigit = lastChar - 'A' + 1;
        } else if (lastChar >= 'J' && lastChar <= 'R') {
            lastDigit = lastChar - 'J' + 1;
            sign = -1;
        } else {
            throw new IllegalArgumentException(
                    "Unexpected overpunch character: '" + lastChar + "' in \"" + raw + "\"");
        }

        String digits = raw.substring(0, raw.length() - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits.trim())
                .movePointLeft(decimalPlaces);
        return sign < 0 ? value.negate() : value;
    }

    /**
     * Format a signed decimal value back into COBOL overpunch notation.
     *
     * @param value         the decimal value
     * @param totalDigits   total display digits (e.g. 12 for PIC S9(10)V99)
     * @param decimalPlaces implied decimal places
     */
    public static String formatSignedDecimal(BigDecimal value, int totalDigits, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().movePointRight(decimalPlaces);
        String digits = String.format("%0" + totalDigits + "d", abs.toBigInteger());

        char lastDigitChar = digits.charAt(digits.length() - 1);
        int lastDigit = lastDigitChar - '0';
        char overpunch;
        if (negative) {
            overpunch = lastDigit == 0 ? '}' : (char) ('J' + lastDigit - 1);
        } else {
            overpunch = lastDigit == 0 ? '{' : (char) ('A' + lastDigit - 1);
        }

        return digits.substring(0, digits.length() - 1) + overpunch;
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) return s;
        return s + " ".repeat(len - s.length());
    }
}
