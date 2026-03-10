package com.carddemo.common.io;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Parses and formats fixed-width fields matching COBOL PIC definitions.
 * <p>
 * COBOL display numeric fields use trailing sign convention in some cases,
 * but for CardDemo the data files use straightforward ASCII representations.
 * </p>
 */
public final class FixedWidthParser {

    private FixedWidthParser() {
        // utility class
    }

    /**
     * Extract a substring from a fixed-width line.
     *
     * @param line   the fixed-width record line
     * @param offset zero-based start position
     * @param length number of characters to extract
     * @return the extracted substring, or spaces if line is too short
     */
    public static String extractField(String line, int offset, int length) {
        if (line == null) {
            return " ".repeat(length);
        }
        int end = Math.min(offset + length, line.length());
        if (offset >= line.length()) {
            return " ".repeat(length);
        }
        String value = line.substring(offset, end);
        // Pad with spaces if extracted value is shorter than expected
        if (value.length() < length) {
            value = value + " ".repeat(length - value.length());
        }
        return value;
    }

    /**
     * Parse a PIC X field (alphanumeric). Returns the value trimmed on the right.
     *
     * @param line   the fixed-width record line
     * @param offset zero-based start position
     * @param length field width
     * @return trimmed string value
     */
    public static String parseAlpha(String line, int offset, int length) {
        return extractField(line, offset, length).stripTrailing();
    }

    /**
     * Parse a PIC 9(n) field (unsigned numeric, stored as display characters).
     * Returns the raw string preserving leading zeros.
     *
     * @param line   the fixed-width record line
     * @param offset zero-based start position
     * @param length field width
     * @return the numeric string with leading zeros preserved
     */
    public static String parseNumericString(String line, int offset, int length) {
        return extractField(line, offset, length).strip();
    }

    /**
     * Parse a PIC S9(n)V99 field (signed decimal, display format).
     * COBOL display format: the sign may be a leading +/- character or
     * the last digit may encode the sign (overpunch). For CardDemo data files
     * the format uses a leading sign character followed by digits with implied decimal.
     *
     * @param line         the fixed-width record line
     * @param offset       zero-based start position
     * @param length       total field width in the file
     * @param decimalPlaces number of implied decimal places (e.g., 2 for V99)
     * @return parsed BigDecimal value
     */
    public static BigDecimal parseSignedDecimal(String line, int offset, int length, int decimalPlaces) {
        String raw = extractField(line, offset, length).strip();
        if (raw.isEmpty()) {
            return BigDecimal.ZERO.setScale(decimalPlaces, RoundingMode.UNNECESSARY);
        }

        // Handle COBOL sign overpunch on last character
        // In EBCDIC-to-ASCII converted files, the last char may encode sign:
        // {=0+, A=1+, B=2+, ..., I=9+, }=0-, J=1-, K=2-, ..., R=9-
        char lastChar = raw.charAt(raw.length() - 1);
        boolean negative = false;
        String digits;

        if (lastChar >= '0' && lastChar <= '9') {
            // No overpunch — might have explicit sign
            if (raw.startsWith("+") || raw.startsWith("-")) {
                negative = raw.startsWith("-");
                digits = raw.substring(1);
            } else {
                digits = raw;
            }
        } else if (lastChar >= '{' && lastChar <= '{' || lastChar >= 'A' && lastChar <= 'I') {
            // Positive overpunch
            int digit = lastChar == '{' ? 0 : (lastChar - 'A' + 1);
            digits = raw.substring(0, raw.length() - 1) + digit;
        } else if (lastChar == '}' || (lastChar >= 'J' && lastChar <= 'R')) {
            // Negative overpunch
            negative = true;
            int digit = lastChar == '}' ? 0 : (lastChar - 'J' + 1);
            digits = raw.substring(0, raw.length() - 1) + digit;
        } else {
            // Fallback: treat as-is
            digits = raw;
        }

        // Remove any remaining non-digit characters except decimal point
        digits = digits.replaceAll("[^0-9.]", "");
        if (digits.isEmpty()) {
            return BigDecimal.ZERO.setScale(decimalPlaces, RoundingMode.UNNECESSARY);
        }

        BigDecimal value;
        if (digits.contains(".")) {
            value = new BigDecimal(digits);
        } else {
            // Implied decimal: last 'decimalPlaces' digits are after the decimal
            value = new BigDecimal(digits).movePointLeft(decimalPlaces);
        }

        if (negative) {
            value = value.negate();
        }
        return value.setScale(decimalPlaces, RoundingMode.HALF_UP);
    }

    /**
     * Format a string value into a fixed-width field, right-padded with spaces.
     *
     * @param value  the value to format
     * @param length the target field width
     * @return right-padded string of exactly 'length' characters
     */
    public static String formatAlpha(String value, int length) {
        if (value == null) {
            return " ".repeat(length);
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return value + " ".repeat(length - value.length());
    }

    /**
     * Format a numeric string into a fixed-width field, left-padded with zeros.
     *
     * @param value  the numeric value string
     * @param length the target field width
     * @return left-padded string of exactly 'length' characters
     */
    public static String formatNumeric(String value, int length) {
        if (value == null) {
            return "0".repeat(length);
        }
        String stripped = value.strip();
        if (stripped.length() >= length) {
            return stripped.substring(stripped.length() - length);
        }
        return "0".repeat(length - stripped.length()) + stripped;
    }

    /**
     * Format a BigDecimal into a signed decimal display field.
     * Uses leading sign character format: +/- followed by zero-padded digits.
     * The total output length is (1 + integerDigits + decimalPlaces).
     *
     * @param value         the decimal value
     * @param integerDigits number of integer digits (e.g., 10 for S9(10))
     * @param decimalPlaces number of decimal places (e.g., 2 for V99)
     * @return formatted string of exactly (1 + integerDigits + decimalPlaces) characters
     */
    public static String formatSignedDecimal(BigDecimal value, int integerDigits, int decimalPlaces) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        value = value.setScale(decimalPlaces, RoundingMode.HALF_UP);
        String sign = value.signum() < 0 ? "-" : "+";
        BigDecimal abs = value.abs();
        String unscaled = abs.movePointRight(decimalPlaces).toBigInteger().toString();
        int totalDigits = integerDigits + decimalPlaces;
        if (unscaled.length() < totalDigits) {
            unscaled = "0".repeat(totalDigits - unscaled.length()) + unscaled;
        }
        return sign + unscaled;
    }

    /**
     * Format a BigDecimal into a fixed-width signed decimal field.
     * The total output is exactly {@code totalWidth} characters.
     * The sign is a leading +/- character, and the remaining characters are
     * zero-padded digits with implied decimal point.
     *
     * @param value      the decimal value
     * @param totalWidth total field width including sign character
     * @param decimalPlaces number of implied decimal places (e.g., 2 for V99)
     * @return formatted string of exactly {@code totalWidth} characters
     */
    public static String formatSignedDecimalFixed(BigDecimal value, int totalWidth, int decimalPlaces) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        value = value.setScale(decimalPlaces, RoundingMode.HALF_UP);
        String sign = value.signum() < 0 ? "-" : "+";
        BigDecimal abs = value.abs();
        String unscaled = abs.movePointRight(decimalPlaces).toBigInteger().toString();
        int digitWidth = totalWidth - 1; // minus sign char
        if (unscaled.length() < digitWidth) {
            unscaled = "0".repeat(digitWidth - unscaled.length()) + unscaled;
        } else if (unscaled.length() > digitWidth) {
            unscaled = unscaled.substring(unscaled.length() - digitWidth);
        }
        return sign + unscaled;
    }

    /**
     * Create a FILLER string of spaces.
     *
     * @param length the filler width
     * @return string of spaces
     */
    public static String filler(int length) {
        return " ".repeat(length);
    }
}
