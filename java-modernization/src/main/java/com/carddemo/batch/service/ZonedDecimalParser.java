package com.carddemo.batch.service;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL zoned-decimal (USAGE IS DISPLAY) fields from ASCII text.
 * <p>
 * In ASCII-encoded COBOL data, signed numeric fields use "overpunch" characters
 * in the last byte to encode both the digit value and the sign:
 * <pre>
 *   Positive: { = +0, A = +1, B = +2, ..., I = +9
 *   Negative: } = -0, J = -1, K = -2, ..., R = -9
 * </pre>
 * This is the convention used by GnuCOBOL and AWS mainframe migration tools
 * when converting EBCDIC zoned-decimal to ASCII.
 */
public final class ZonedDecimalParser {

    private static final String POSITIVE_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEGATIVE_OVERPUNCH = "}JKLMNOPQR";

    private ZonedDecimalParser() {
        // utility class
    }

    /**
     * Parses a COBOL zoned-decimal string into a {@link BigDecimal}.
     *
     * @param raw           the raw zoned-decimal string (e.g., "00000001940{")
     * @param decimalPlaces the number of implied decimal places (V99 = 2)
     * @return the parsed numeric value
     */
    public static BigDecimal parse(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        int sign = 1;
        int lastDigit;

        int posIdx = POSITIVE_OVERPUNCH.indexOf(lastChar);
        if (posIdx >= 0) {
            lastDigit = posIdx;
        } else {
            int negIdx = NEGATIVE_OVERPUNCH.indexOf(lastChar);
            if (negIdx >= 0) {
                sign = -1;
                lastDigit = negIdx;
            } else if (Character.isDigit(lastChar)) {
                // No overpunch — treat as unsigned
                lastDigit = lastChar - '0';
            } else {
                throw new IllegalArgumentException(
                        "Invalid overpunch character: '" + lastChar + "' in '" + raw + "'");
            }
        }

        String fullDigits = digits + lastDigit;
        BigDecimal value = new BigDecimal(fullDigits, MathContext.UNLIMITED);

        if (decimalPlaces > 0) {
            value = value.movePointLeft(decimalPlaces);
        }

        return sign < 0 ? value.negate() : value;
    }
}
