package com.carddemo.util;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL DISPLAY-format signed numeric fields (trailing overpunch sign).
 *
 * In COBOL PIC S9(n)V99 DISPLAY format, the sign is encoded in the last byte:
 * <pre>
 *   '{' = +0   'A'..'I' = +1..+9
 *   '}' = -0   'J'..'R' = -1..-9
 * </pre>
 *
 * The implied decimal point (V) is not stored; it is determined by the picture.
 */
public final class CobolNumericParser {

    private CobolNumericParser() {}

    /**
     * Parses a COBOL signed display numeric field.
     *
     * @param raw            the raw character data from the file
     * @param integerDigits  number of integer digits (the 'n' in S9(n))
     * @param decimalDigits  number of decimal digits (the 'm' in V9(m))
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parseSignedDisplay(String raw, int integerDigits, int decimalDigits) {
        int totalDigits = integerDigits + decimalDigits;
        if (raw.length() != totalDigits) {
            throw new IllegalArgumentException(
                    "Expected " + totalDigits + " characters but got " + raw.length() + ": '" + raw + "'");
        }

        char lastChar = raw.charAt(raw.length() - 1);
        int lastDigit;
        boolean negative;

        if (lastChar >= '0' && lastChar <= '9') {
            lastDigit = lastChar - '0';
            negative = false;
        } else if (lastChar == '{') {
            lastDigit = 0;
            negative = false;
        } else if (lastChar == '}') {
            lastDigit = 0;
            negative = true;
        } else if (lastChar >= 'A' && lastChar <= 'I') {
            lastDigit = lastChar - 'A' + 1;
            negative = false;
        } else if (lastChar >= 'J' && lastChar <= 'R') {
            lastDigit = lastChar - 'J' + 1;
            negative = true;
        } else {
            throw new IllegalArgumentException("Invalid overpunch character: '" + lastChar + "'");
        }

        String digits = raw.substring(0, raw.length() - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits).movePointLeft(decimalDigits);
        return negative ? value.negate() : value;
    }

    /**
     * Formats a BigDecimal back to COBOL signed display format with trailing overpunch.
     *
     * @param value          the value to format
     * @param integerDigits  number of integer digits
     * @param decimalDigits  number of decimal digits
     * @return the formatted string with trailing overpunch sign
     */
    public static String formatSignedDisplay(BigDecimal value, int integerDigits, int decimalDigits) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();

        BigDecimal scaled = abs.movePointRight(decimalDigits);
        String digits = scaled.toBigInteger().toString();

        int totalDigits = integerDigits + decimalDigits;
        if (digits.length() > totalDigits) {
            throw new ArithmeticException("Value " + value + " exceeds PIC S9(" + integerDigits + ")V9(" + decimalDigits + ")");
        }
        digits = "0".repeat(totalDigits - digits.length()) + digits;

        int lastDigitVal = digits.charAt(totalDigits - 1) - '0';
        char overpunch;
        if (negative) {
            overpunch = lastDigitVal == 0 ? '}' : (char) ('J' + lastDigitVal - 1);
        } else {
            overpunch = lastDigitVal == 0 ? '{' : (char) ('A' + lastDigitVal - 1);
        }
        return digits.substring(0, totalDigits - 1) + overpunch;
    }
}
