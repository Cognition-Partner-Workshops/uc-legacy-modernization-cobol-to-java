package com.carddemo.batch.util;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL zoned-decimal (DISPLAY) numeric fields stored in ASCII text files.
 * <p>
 * In COBOL zoned decimal, the sign is embedded in the last byte of the field:
 * <ul>
 *   <li>Positive: {@code {} = +0, A = +1, B = +2, ... I = +9</li>
 *   <li>Negative: {@code }} = -0, J = -1, K = -2, ... R = -9</li>
 * </ul>
 * The implied decimal position is determined by the PIC clause (e.g. V99 = 2 decimal places).
 */
public final class ZonedDecimalParser {

    private static final String POSITIVE_SIGNS = "{ABCDEFGHI";
    private static final String NEGATIVE_SIGNS = "}JKLMNOPQR";

    private ZonedDecimalParser() {
    }

    /**
     * Parses a COBOL zoned-decimal string into a {@link BigDecimal}.
     *
     * @param raw           the raw fixed-width string from the data file
     * @param decimalPlaces number of implied decimal places (e.g. 2 for PIC V99)
     * @return the parsed decimal value
     * @throws IllegalArgumentException if the sign character is unrecognised
     */
    public static BigDecimal parse(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        String trimmed = raw;
        char lastChar = trimmed.charAt(trimmed.length() - 1);

        int sign;
        int lastDigit;

        int posIdx = POSITIVE_SIGNS.indexOf(lastChar);
        if (posIdx >= 0) {
            sign = 1;
            lastDigit = posIdx;
        } else {
            int negIdx = NEGATIVE_SIGNS.indexOf(lastChar);
            if (negIdx >= 0) {
                sign = -1;
                lastDigit = negIdx;
            } else if (Character.isDigit(lastChar)) {
                sign = 1;
                lastDigit = lastChar - '0';
            } else {
                throw new IllegalArgumentException(
                        "Unrecognised zoned-decimal sign character: '" + lastChar + "' (0x"
                                + Integer.toHexString(lastChar) + ")");
            }
        }

        String digits = trimmed.substring(0, trimmed.length() - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits);

        if (decimalPlaces > 0) {
            value = value.movePointLeft(decimalPlaces);
        }

        if (sign < 0) {
            value = value.negate();
        }

        return value;
    }
}
