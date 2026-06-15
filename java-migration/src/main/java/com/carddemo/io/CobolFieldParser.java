package com.carddemo.io;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL zoned-decimal fields from ASCII text representation.
 * Handles sign-overpunch encoding where the last character encodes both
 * the sign and the final digit.
 *
 * <p>Overpunch mapping (EBCDIC-to-ASCII convention used in CardDemo data files):
 * <ul>
 *   <li>Positive: '{' = 0, 'A'-'I' = 1-9</li>
 *   <li>Negative: '}' = 0, 'J'-'R' = 1-9</li>
 * </ul>
 */
public final class CobolFieldParser {

    private CobolFieldParser() {}

    /**
     * Parses a signed zoned-decimal field (PIC S9(m)V9(n)) from its ASCII text form.
     *
     * @param raw           the raw string from the fixed-width record
     * @param decimalPlaces number of implied decimal places (digits after V)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parseSignedDecimal(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        final char lastChar = raw.charAt(raw.length() - 1);
        final int sign;
        final int lastDigit;

        if (lastChar >= '0' && lastChar <= '9') {
            sign = 1;
            lastDigit = lastChar - '0';
        } else if (lastChar == '{') {
            sign = 1;
            lastDigit = 0;
        } else if (lastChar >= 'A' && lastChar <= 'I') {
            sign = 1;
            lastDigit = lastChar - 'A' + 1;
        } else if (lastChar == '}') {
            sign = -1;
            lastDigit = 0;
        } else if (lastChar >= 'J' && lastChar <= 'R') {
            sign = -1;
            lastDigit = lastChar - 'J' + 1;
        } else {
            throw new IllegalArgumentException(
                    "Invalid overpunch character: '" + lastChar + "' in field: " + raw);
        }

        final String digits = raw.substring(0, raw.length() - 1) + lastDigit;
        final BigDecimal unscaled = new BigDecimal(digits, MathContext.UNLIMITED);
        final BigDecimal scaled = unscaled.movePointLeft(decimalPlaces);
        return sign < 0 ? scaled.negate() : scaled;
    }

    /**
     * Parses an unsigned numeric field (PIC 9(n)).
     */
    public static String parseUnsignedNumeric(String raw) {
        return raw;
    }

    /**
     * Parses an alphanumeric field (PIC X(n)), trimming trailing spaces.
     */
    public static String parseAlphanumeric(String raw) {
        if (raw == null) return "";
        int end = raw.length();
        while (end > 0 && raw.charAt(end - 1) == ' ') {
            end--;
        }
        return raw.substring(0, end);
    }

    /**
     * Formats a BigDecimal as a signed zoned-decimal string with overpunch encoding.
     * Used for writing output files that match COBOL display format.
     *
     * @param value         the value to format
     * @param totalDigits   total digit positions (m + n for PIC S9(m)V9(n))
     * @param decimalPlaces number of implied decimal places
     * @return the formatted string with sign overpunch on last character
     */
    public static String formatSignedDecimal(BigDecimal value, int totalDigits, int decimalPlaces) {
        final boolean negative = value.signum() < 0;
        final BigDecimal absValue = value.abs().movePointRight(decimalPlaces);
        final String digits = String.format("%0" + totalDigits + "d", absValue.toBigInteger());

        final int lastDigit = digits.charAt(digits.length() - 1) - '0';
        final char overpunch;
        if (negative) {
            overpunch = lastDigit == 0 ? '}' : (char) ('J' + lastDigit - 1);
        } else {
            overpunch = lastDigit == 0 ? '{' : (char) ('A' + lastDigit - 1);
        }

        return digits.substring(0, digits.length() - 1) + overpunch;
    }

    /**
     * Formats a BigDecimal as a packed-decimal (COMP-3) byte array.
     * COMP-3 stores two digits per byte with the sign nibble in the last half-byte.
     *
     * @param value         the value to format
     * @param totalDigits   total digit positions (m + n for PIC S9(m)V9(n))
     * @param decimalPlaces number of implied decimal places
     * @return the packed-decimal byte array
     */
    public static byte[] formatComp3(BigDecimal value, int totalDigits, int decimalPlaces) {
        final boolean negative = value.signum() < 0;
        final BigDecimal absValue = value.abs().movePointRight(decimalPlaces);
        final String digits = String.format("%0" + totalDigits + "d", absValue.toBigInteger());

        final int byteCount = (totalDigits + 2) / 2;
        final byte[] packed = new byte[byteCount];

        final String paddedDigits = (totalDigits % 2 == 0 ? "0" : "") + digits;

        for (int i = 0; i < paddedDigits.length(); i++) {
            final int digit = paddedDigits.charAt(i) - '0';
            final int byteIndex = i / 2;
            if (i % 2 == 0) {
                packed[byteIndex] = (byte) (digit << 4);
            } else {
                packed[byteIndex] |= (byte) digit;
            }
        }

        final int signNibble = negative ? 0x0D : 0x0C;
        packed[byteCount - 1] = (byte) ((packed[byteCount - 1] & 0xF0) | signNibble);

        return packed;
    }

    /**
     * Formats an unsigned numeric value right-justified and zero-filled.
     */
    public static String formatUnsignedNumeric(String value, int width) {
        return String.format("%" + width + "s", value).replace(' ', '0');
    }

    /**
     * Formats an alphanumeric value left-justified and space-filled.
     */
    public static String formatAlphanumeric(String value, int width) {
        if (value == null) value = "";
        if (value.length() >= width) return value.substring(0, width);
        return value + " ".repeat(width - value.length());
    }
}
