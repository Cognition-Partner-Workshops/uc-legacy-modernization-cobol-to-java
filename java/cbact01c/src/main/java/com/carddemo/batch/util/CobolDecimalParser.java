package com.carddemo.batch.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Utilities for parsing COBOL zoned-decimal values that use the
 * EBCDIC overpunch sign convention in their last character.
 *
 * <p>In the ASCII sample data the trailing overpunch characters are:
 * <ul>
 *   <li>{@code {  } = +0</li>
 *   <li>{@code A-I} = +1 through +9</li>
 *   <li>{@code }  } = -0</li>
 *   <li>{@code J-R} = -1 through -9</li>
 * </ul>
 *
 * <p>This handles {@code PIC S9(n)V99} by treating the last two digits
 * (before overpunch decoding) as the two implied-decimal positions.
 */
public final class CobolDecimalParser {

    private CobolDecimalParser() { }

    // Maps the overpunch character to its digit and sign
    private static final Map<Character, int[]> OVERPUNCH = Map.ofEntries(
            Map.entry('{', new int[]{0, +1}),
            Map.entry('A', new int[]{1, +1}),
            Map.entry('B', new int[]{2, +1}),
            Map.entry('C', new int[]{3, +1}),
            Map.entry('D', new int[]{4, +1}),
            Map.entry('E', new int[]{5, +1}),
            Map.entry('F', new int[]{6, +1}),
            Map.entry('G', new int[]{7, +1}),
            Map.entry('H', new int[]{8, +1}),
            Map.entry('I', new int[]{9, +1}),
            Map.entry('}', new int[]{0, -1}),
            Map.entry('J', new int[]{1, -1}),
            Map.entry('K', new int[]{2, -1}),
            Map.entry('L', new int[]{3, -1}),
            Map.entry('M', new int[]{4, -1}),
            Map.entry('N', new int[]{5, -1}),
            Map.entry('O', new int[]{6, -1}),
            Map.entry('P', new int[]{7, -1}),
            Map.entry('Q', new int[]{8, -1}),
            Map.entry('R', new int[]{9, -1})
    );

    /**
     * Parses a COBOL signed zoned-decimal field with implied decimal places.
     *
     * @param raw            the raw string from the file (e.g. "00000001940{")
     * @param decimalPlaces  number of implied decimal places (e.g. 2 for V99)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parseSignedDecimal(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        int[] overpunch = OVERPUNCH.get(lastChar);

        String digits;
        int sign;

        if (overpunch != null) {
            // Replace the overpunch character with its digit
            digits = raw.substring(0, raw.length() - 1) + overpunch[0];
            sign = overpunch[1];
        } else {
            // No overpunch — assume positive, all digits
            digits = raw;
            sign = 1;
        }

        BigDecimal value = new BigDecimal(digits);
        if (decimalPlaces > 0) {
            value = value.movePointLeft(decimalPlaces);
        }
        if (sign < 0) {
            value = value.negate();
        }

        return value.setScale(decimalPlaces, RoundingMode.HALF_UP);
    }

    /**
     * Formats a BigDecimal as a COBOL signed zoned-decimal string
     * with trailing overpunch character.
     *
     * @param value          the value to format
     * @param totalDigits    total digits including decimal (e.g. 12 for S9(10)V99)
     * @param decimalPlaces  implied decimal places
     * @return the formatted string with trailing overpunch
     */
    public static String formatSignedDecimal(BigDecimal value, int totalDigits, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal absValue = value.abs().setScale(decimalPlaces, RoundingMode.HALF_UP);

        // Move decimal point right to get integer representation
        BigDecimal shifted = absValue.movePointRight(decimalPlaces);
        String digits = shifted.toBigInteger().toString();

        // Pad with leading zeros to fill totalDigits
        while (digits.length() < totalDigits) {
            digits = "0" + digits;
        }

        // Replace last digit with overpunch character
        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        char overpunchChar;
        if (negative) {
            overpunchChar = switch (lastDigit) {
                case 0 -> '}';
                case 1 -> 'J';
                case 2 -> 'K';
                case 3 -> 'L';
                case 4 -> 'M';
                case 5 -> 'N';
                case 6 -> 'O';
                case 7 -> 'P';
                case 8 -> 'Q';
                case 9 -> 'R';
                default -> throw new IllegalStateException("Unexpected digit: " + lastDigit);
            };
        } else {
            overpunchChar = switch (lastDigit) {
                case 0 -> '{';
                case 1 -> 'A';
                case 2 -> 'B';
                case 3 -> 'C';
                case 4 -> 'D';
                case 5 -> 'E';
                case 6 -> 'F';
                case 7 -> 'G';
                case 8 -> 'H';
                case 9 -> 'I';
                default -> throw new IllegalStateException("Unexpected digit: " + lastDigit);
            };
        }

        return digits.substring(0, digits.length() - 1) + overpunchChar;
    }
}
