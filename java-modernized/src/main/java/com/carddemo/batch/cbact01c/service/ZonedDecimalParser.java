package com.carddemo.batch.cbact01c.service;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL zoned-decimal (DISPLAY format) values from their ASCII
 * representation, as found in the CardDemo ASCII data files.
 *
 * <p>In COBOL zoned-decimal, the sign is encoded in the last byte using an
 * "overpunch" character. The ASCII-file convention used by CardDemo is:
 * <ul>
 *   <li>{@code {  } = +0,  {@code A}-{@code I} = +1 to +9</li>
 *   <li>{@code }} = -0,  {@code J}-{@code R} = -1 to -9</li>
 * </ul>
 *
 * <p>Example: For {@code PIC S9(10)V99}, the field width is 12 characters.
 * {@code "00000001940{"} decodes to +000000019400 with implied decimal
 * at V99 giving {@code 194.00}.
 */
public final class ZonedDecimalParser {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private ZonedDecimalParser() {
        // utility class
    }

    /**
     * Parses a zoned-decimal string with an implied decimal point.
     *
     * @param raw            the raw zoned-decimal string from the file
     * @param decimalPlaces  number of implied decimal places (e.g. 2 for V99)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parse(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String digitsPart = raw.substring(0, raw.length() - 1);
        int lastDigit;
        boolean negative;

        int posIdx = POSITIVE_OVERPUNCH.indexOf(lastChar);
        if (posIdx >= 0) {
            lastDigit = posIdx;
            negative = false;
        } else {
            int negIdx = NEGATIVE_OVERPUNCH.indexOf(lastChar);
            if (negIdx >= 0) {
                lastDigit = negIdx;
                negative = true;
            } else if (Character.isDigit(lastChar)) {
                // No overpunch — treat as unsigned positive
                lastDigit = lastChar - '0';
                negative = false;
            } else {
                throw new IllegalArgumentException(
                        "Invalid zoned-decimal overpunch character: '" + lastChar
                                + "' in value: " + raw);
            }
        }

        String allDigits = digitsPart + lastDigit;
        BigDecimal value = new BigDecimal(allDigits)
                .movePointLeft(decimalPlaces);

        return negative ? value.negate() : value;
    }

    /**
     * Formats a BigDecimal back into a zoned-decimal string with the
     * specified total width and implied decimal places.
     *
     * @param value         the value to format
     * @param totalWidth    total character width of the field
     * @param decimalPlaces implied decimal places
     * @return the zoned-decimal string with overpunch sign character
     */
    public static String format(BigDecimal value, int totalWidth, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal absValue = value.abs();

        // Scale to remove the decimal point: e.g. 194.00 with V99 -> 19400
        long unscaled = absValue.movePointRight(decimalPlaces)
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();

        String digits = String.format("%0" + totalWidth + "d", unscaled);
        if (digits.length() > totalWidth) {
            throw new ArithmeticException(
                    "Value " + value + " exceeds field width " + totalWidth);
        }

        // Replace last digit with overpunch character
        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        String overpunch = negative ? NEGATIVE_OVERPUNCH : POSITIVE_OVERPUNCH;
        return digits.substring(0, digits.length() - 1) + overpunch.charAt(lastDigit);
    }
}
