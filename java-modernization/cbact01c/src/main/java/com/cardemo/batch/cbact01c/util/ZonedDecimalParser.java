package com.cardemo.batch.cbact01c.util;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses and formats COBOL zoned-decimal (DISPLAY) signed numerics.
 *
 * <p>In ASCII transfer files the sign is encoded as an <em>overpunch</em>
 * on the last byte:
 * <pre>
 *   Positive: { = 0, A = 1, B = 2 … I = 9
 *   Negative: } = 0, J = 1, K = 2 … R = 9
 * </pre>
 */
public final class ZonedDecimalParser {

    private static final String POS_DIGITS = "{ABCDEFGHI";
    private static final String NEG_DIGITS = "}JKLMNOPQR";

    private ZonedDecimalParser() {}

    /**
     * Parse a zoned-decimal string into a {@link BigDecimal}.
     *
     * @param raw           the raw character data (e.g. {@code "00000001940{"})
     * @param decimalPlaces number of implied decimal places (the COBOL V-count)
     * @return the numeric value
     */
    public static BigDecimal parse(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("Empty zoned decimal input");
        }

        char lastChar = raw.charAt(raw.length() - 1);
        int sign;
        int lastDigit;

        int posIdx = POS_DIGITS.indexOf(lastChar);
        if (posIdx >= 0) {
            sign = 1;
            lastDigit = posIdx;
        } else {
            int negIdx = NEG_DIGITS.indexOf(lastChar);
            if (negIdx >= 0) {
                sign = -1;
                lastDigit = negIdx;
            } else if (Character.isDigit(lastChar)) {
                sign = 1;
                lastDigit = lastChar - '0';
            } else {
                throw new IllegalArgumentException(
                        "Invalid overpunch character: " + lastChar);
            }
        }

        String digits = raw.substring(0, raw.length() - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits, MathContext.UNLIMITED);
        if (decimalPlaces > 0) {
            value = value.movePointLeft(decimalPlaces);
        }
        return sign < 0 ? value.negate() : value;
    }

    /**
     * Format a {@link BigDecimal} as a zoned-decimal string with overpunch sign.
     *
     * @param value         the value to format
     * @param totalDigits   total number of digits (integer + decimal)
     * @param decimalPlaces number of implied decimal places
     * @return the zoned-decimal string
     */
    public static String format(BigDecimal value, int totalDigits, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();

        BigDecimal shifted = abs.movePointRight(decimalPlaces);
        long intValue = shifted.setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
        String digits = String.format("%0" + totalDigits + "d", intValue);

        if (digits.length() > totalDigits) {
            throw new ArithmeticException(
                    "Value too large for PIC S9(" + (totalDigits - decimalPlaces)
                            + ")V" + "9".repeat(decimalPlaces) + ": " + value);
        }

        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        char overpunch = negative
                ? NEG_DIGITS.charAt(lastDigit)
                : POS_DIGITS.charAt(lastDigit);

        return digits.substring(0, digits.length() - 1) + overpunch;
    }
}
