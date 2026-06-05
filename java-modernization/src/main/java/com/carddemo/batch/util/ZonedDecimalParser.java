package com.carddemo.batch.util;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL zoned-decimal (DISPLAY) fields in their ASCII text
 * representation.
 *
 * <p>In a zoned-decimal field the last byte carries both the least-significant
 * digit and the sign of the number via an "overpunch" character:</p>
 *
 * <pre>
 *   Positive:  { = 0, A = 1, B = 2, C = 3, D = 4,
 *               E = 5, F = 6, G = 7, H = 8, I = 9
 *   Negative:  } = 0, J = 1, K = 2, L = 3, M = 4,
 *               N = 5, O = 6, P = 7, Q = 8, R = 9
 * </pre>
 */
public final class ZonedDecimalParser {

    private static final String POS_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEG_OVERPUNCH = "}JKLMNOPQR";

    private ZonedDecimalParser() {}

    /**
     * Parse a zoned-decimal string into a {@link BigDecimal}.
     *
     * @param raw            the raw fixed-width text (e.g. {@code "00000001940{"})
     * @param impliedScale   number of implied decimal places (V99 → 2)
     * @return the parsed value
     */
    public static BigDecimal parse(String raw, int impliedScale) {
        if (raw == null || raw.isEmpty()) {
            throw new IllegalArgumentException("Input must not be null or empty");
        }

        char lastChar = raw.charAt(raw.length() - 1);
        int digit;
        boolean negative;

        int posIdx = POS_OVERPUNCH.indexOf(lastChar);
        if (posIdx >= 0) {
            digit = posIdx;
            negative = false;
        } else {
            int negIdx = NEG_OVERPUNCH.indexOf(lastChar);
            if (negIdx >= 0) {
                digit = negIdx;
                negative = true;
            } else if (Character.isDigit(lastChar)) {
                digit = lastChar - '0';
                negative = false;
            } else {
                throw new IllegalArgumentException(
                        "Unrecognised overpunch character: '" + lastChar + "'");
            }
        }

        String digits = raw.substring(0, raw.length() - 1) + digit;
        BigDecimal value = new BigDecimal(digits)
                .movePointLeft(impliedScale);

        return negative ? value.negate() : value;
    }

    /**
     * Format a {@link BigDecimal} back into a zoned-decimal string with
     * overpunch sign encoding.
     *
     * @param value       the value to format
     * @param totalDigits total number of digits (integer + decimal combined)
     * @param scale       number of implied decimal places
     * @return the formatted zoned-decimal string
     */
    public static String format(BigDecimal value, int totalDigits, int scale) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().movePointRight(scale);
        String digits = abs.toBigInteger().toString();

        if (digits.length() > totalDigits) {
            throw new IllegalArgumentException(
                    "Value " + value + " exceeds PIC S9(" + (totalDigits - scale)
                    + ")V9(" + scale + ")");
        }

        digits = "0".repeat(totalDigits - digits.length()) + digits;

        int lastDigit = digits.charAt(digits.length() - 1) - '0';
        String overpunch = negative
                ? String.valueOf(NEG_OVERPUNCH.charAt(lastDigit))
                : String.valueOf(POS_OVERPUNCH.charAt(lastDigit));

        return digits.substring(0, digits.length() - 1) + overpunch;
    }
}
