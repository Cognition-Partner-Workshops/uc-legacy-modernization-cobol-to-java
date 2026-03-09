package com.carddemo.batch.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Parses COBOL zoned-decimal (DISPLAY) numeric fields from their ASCII
 * text representation into Java {@link BigDecimal} values.
 *
 * <p>In the ASCII sample data, signed fields use an overpunch convention
 * where the last character encodes both the final digit and the sign:
 * <ul>
 *   <li>{@code {  = +0}, {@code A = +1}, ... {@code I = +9}</li>
 *   <li>{@code } = -0}, {@code J = -1}, ... {@code R = -9}</li>
 * </ul>
 *
 * <p>For PIC S9(10)V99, the field is 12 characters wide; the implied
 * decimal point sits before the last two digits.
 */
public final class CobolDecimalParser {

    private CobolDecimalParser() {
        // utility class
    }

    /**
     * Parses a COBOL signed zoned-decimal field (PIC S9(m)V9(n)).
     *
     * @param raw           the raw text (e.g. "00000001940{")
     * @param scale         number of implied decimal places (e.g. 2 for V99)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parseSignedDecimal(String raw, int scale) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.UNNECESSARY);
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.UNNECESSARY);
        }

        char lastChar = trimmed.charAt(trimmed.length() - 1);
        int sign = 1;
        int lastDigit;

        if (lastChar >= '0' && lastChar <= '9') {
            lastDigit = lastChar - '0';
        } else {
            int[] decoded = decodeOverpunch(lastChar);
            sign = decoded[0];
            lastDigit = decoded[1];
        }

        String digits = trimmed.substring(0, trimmed.length() - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits).movePointLeft(scale);
        return sign < 0 ? value.negate() : value;
    }

    /**
     * Parses an unsigned numeric field (PIC 9(n)).
     *
     * @param raw the raw text
     * @return the parsed long value
     */
    public static long parseUnsignedLong(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        return Long.parseLong(raw.trim());
    }

    /**
     * Formats a BigDecimal as a COBOL signed zoned-decimal string with
     * trailing overpunch character.
     *
     * @param value     the value to format
     * @param totalLen  total character width (e.g. 12 for PIC S9(10)V99)
     * @param scale     implied decimal places
     * @return the formatted string
     */
    public static String formatSignedDecimal(BigDecimal value, int totalLen, int scale) {
        BigDecimal scaled = value.setScale(scale, RoundingMode.HALF_UP);
        boolean negative = scaled.signum() < 0;
        BigDecimal abs = scaled.abs();

        // Move to integer representation: e.g. 1940.00 -> 194000
        long intValue = abs.movePointRight(scale).longValueExact();
        String digits = String.format("%0" + totalLen + "d", intValue);

        // Ensure correct length
        if (digits.length() > totalLen) {
            digits = digits.substring(digits.length() - totalLen);
        }

        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        char overpunch = encodeOverpunch(negative, lastDigit);
        return digits.substring(0, digits.length() - 1) + overpunch;
    }

    /**
     * Formats an unsigned long as a zero-padded string.
     *
     * @param value the value
     * @param width the total width
     * @return zero-padded string
     */
    public static String formatUnsignedLong(long value, int width) {
        return String.format("%0" + width + "d", value);
    }

    private static int[] decodeOverpunch(char c) {
        return switch (c) {
            case '{' -> new int[]{1, 0};
            case 'A' -> new int[]{1, 1};
            case 'B' -> new int[]{1, 2};
            case 'C' -> new int[]{1, 3};
            case 'D' -> new int[]{1, 4};
            case 'E' -> new int[]{1, 5};
            case 'F' -> new int[]{1, 6};
            case 'G' -> new int[]{1, 7};
            case 'H' -> new int[]{1, 8};
            case 'I' -> new int[]{1, 9};
            case '}' -> new int[]{-1, 0};
            case 'J' -> new int[]{-1, 1};
            case 'K' -> new int[]{-1, 2};
            case 'L' -> new int[]{-1, 3};
            case 'M' -> new int[]{-1, 4};
            case 'N' -> new int[]{-1, 5};
            case 'O' -> new int[]{-1, 6};
            case 'P' -> new int[]{-1, 7};
            case 'Q' -> new int[]{-1, 8};
            case 'R' -> new int[]{-1, 9};
            default -> throw new IllegalArgumentException(
                    "Unknown overpunch character: '" + c + "' (0x" +
                            Integer.toHexString(c) + ")");
        };
    }

    private static char encodeOverpunch(boolean negative, int digit) {
        if (negative) {
            return switch (digit) {
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
                default -> throw new IllegalArgumentException("Invalid digit: " + digit);
            };
        } else {
            return switch (digit) {
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
                default -> throw new IllegalArgumentException("Invalid digit: " + digit);
            };
        }
    }
}
