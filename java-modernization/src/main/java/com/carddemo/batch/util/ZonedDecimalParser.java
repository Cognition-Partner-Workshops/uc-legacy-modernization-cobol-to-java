package com.carddemo.batch.util;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL zoned-decimal (DISPLAY) fields that use overpunch sign encoding.
 * <p>
 * In COBOL ASCII files the sign is embedded in the last byte of a numeric field
 * using the following overpunch convention:
 * <pre>
 *   Positive: { = 0, A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9
 *   Negative: } = 0, J = 1, K = 2, L = 3, M = 4, N = 5, O = 6, P = 7, Q = 8, R = 9
 * </pre>
 * For unsigned fields the last character is simply a digit '0'-'9'.
 */
public final class ZonedDecimalParser {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private ZonedDecimalParser() {
        // utility class
    }

    /**
     * Parses a COBOL zoned-decimal string into a {@link BigDecimal}.
     *
     * @param raw            the raw fixed-width string from the data file
     * @param decimalPlaces  number of implied decimal places (V99 = 2)
     * @return parsed value as {@link BigDecimal}
     * @throws IllegalArgumentException if the input is null, empty, or contains
     *                                  an unrecognised overpunch character
     */
    public static BigDecimal parse(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("Zoned-decimal input must not be null or empty");
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String prefix = raw.substring(0, raw.length() - 1);

        int digit;
        boolean negative;

        int posIdx = POSITIVE_OVERPUNCH.indexOf(lastChar);
        if (posIdx >= 0) {
            digit = posIdx;
            negative = false;
        } else {
            int negIdx = NEGATIVE_OVERPUNCH.indexOf(lastChar);
            if (negIdx >= 0) {
                digit = negIdx;
                negative = true;
            } else if (Character.isDigit(lastChar)) {
                // unsigned field – last char is just a digit
                digit = lastChar - '0';
                negative = false;
            } else {
                throw new IllegalArgumentException(
                        "Unrecognised overpunch character: '" + lastChar + "' (0x"
                                + Integer.toHexString(lastChar) + ")");
            }
        }

        String digits = prefix + digit;
        BigDecimal value = new BigDecimal(digits).movePointLeft(decimalPlaces);
        return negative ? value.negate() : value;
    }

    /**
     * Formats a {@link BigDecimal} value back to a COBOL zoned-decimal string
     * with positive overpunch encoding.
     *
     * @param value          the numeric value
     * @param totalDigits    total number of digits (integer + decimal combined)
     * @param decimalPlaces  number of implied decimal places
     * @return fixed-width zoned-decimal string with overpunch sign
     */
    public static String format(BigDecimal value, int totalDigits, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().movePointRight(decimalPlaces);
        String digits = abs.toBigInteger().toString();

        // left-pad with zeros to totalDigits
        while (digits.length() < totalDigits) {
            digits = "0" + digits;
        }

        char lastDigitChar = digits.charAt(digits.length() - 1);
        int lastDigit = lastDigitChar - '0';
        String overpunch = negative ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH;
        char signChar = overpunch.charAt(lastDigit);

        return digits.substring(0, digits.length() - 1) + signChar;
    }
}
