package com.carddemo.batch.io;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL zoned-decimal (DISPLAY) fields from ASCII text representation.
 *
 * In COBOL, a signed zoned-decimal field (e.g. PIC S9(10)V99) stores
 * the sign as an "overpunch" on the last character:
 * <ul>
 *   <li>{@code {  = +0}, {@code A-I = +1..+9}</li>
 *   <li>{@code }  = -0}, {@code J-R = -1..-9}</li>
 * </ul>
 */
public final class CobolDecimalParser {

    private CobolDecimalParser() {
    }

    /**
     * Parses a COBOL zoned-decimal ASCII string into a BigDecimal.
     *
     * @param raw            the raw character data (e.g. "00000001940{")
     * @param decimalPlaces  implied decimal digits (V99 = 2)
     * @return the numeric value
     */
    public static BigDecimal parseZonedDecimal(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String prefix = raw.substring(0, raw.length() - 1);

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
                if (Character.isDigit(lastChar)) {
                    lastDigit = lastChar - '0';
                    negative = false;
                } else {
                    throw new IllegalArgumentException(
                            "Invalid overpunch character: '" + lastChar + "' in \"" + raw + "\"");
                }
            }
        }

        String digits = prefix + lastDigit;
        BigDecimal unscaled = new BigDecimal(digits, MathContext.UNLIMITED);
        BigDecimal scaled = unscaled.movePointLeft(decimalPlaces);
        return negative ? scaled.negate() : scaled;
    }

    /**
     * Formats a BigDecimal back to COBOL zoned-decimal ASCII representation.
     *
     * @param value         the numeric value
     * @param totalDigits   total display digits (integer + decimal)
     * @param decimalPlaces implied decimal digits
     * @return the zoned-decimal string with overpunch sign
     */
    public static String formatZonedDecimal(BigDecimal value, int totalDigits, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().movePointRight(decimalPlaces);
        long unscaled = abs.setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();

        String digits = String.format("%0" + totalDigits + "d", unscaled);
        if (digits.length() > totalDigits) {
            throw new ArithmeticException("Value " + value + " exceeds PIC S9(" +
                    (totalDigits - decimalPlaces) + ")V" + "9".repeat(decimalPlaces));
        }

        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        char overpunch = overpunchChar(lastDigit, negative);
        return digits.substring(0, digits.length() - 1) + overpunch;
    }

    private static char overpunchChar(int digit, boolean negative) {
        if (negative) {
            return digit == 0 ? '}' : (char) ('J' + digit - 1);
        } else {
            return digit == 0 ? '{' : (char) ('A' + digit - 1);
        }
    }
}
