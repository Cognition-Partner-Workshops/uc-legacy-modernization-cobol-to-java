package com.carddemo.batch.cbact01c.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Parses COBOL zoned-decimal (DISPLAY) signed numerics from ASCII text.
 * <p>
 * In COBOL, a PIC S9(10)V99 DISPLAY field is stored as 12 characters where
 * the sign is encoded in the last byte via an "overpunch" character:
 * <pre>
 *   Positive: {=0, A=1, B=2, C=3, D=4, E=5, F=6, G=7, H=8, I=9
 *   Negative: }=0, J=1, K=2, L=3, M=4, N=5, O=6, P=7, Q=8, R=9
 * </pre>
 * The V (implied decimal) is not stored; it is defined by the PIC clause.
 */
public final class CobolDecimalParser {

    private CobolDecimalParser() {}

    private static final String POS_OVERPUNCH = "{ABCDEFGHI";
    private static final String NEG_OVERPUNCH = "}JKLMNOPQR";

    /**
     * Parses a COBOL zoned-decimal string with implied decimal places.
     *
     * @param raw           the raw character data (e.g., "00000001940{")
     * @param decimalPlaces number of implied decimal places (V99 = 2)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parse(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = trimmed.charAt(trimmed.length() - 1);
        String digits;
        boolean negative;

        int posIdx = POS_OVERPUNCH.indexOf(lastChar);
        int negIdx = NEG_OVERPUNCH.indexOf(lastChar);

        if (posIdx >= 0) {
            digits = trimmed.substring(0, trimmed.length() - 1) + posIdx;
            negative = false;
        } else if (negIdx >= 0) {
            digits = trimmed.substring(0, trimmed.length() - 1) + negIdx;
            negative = true;
        } else if (Character.isDigit(lastChar)) {
            digits = trimmed;
            negative = false;
        } else {
            throw new IllegalArgumentException(
                    "Invalid COBOL zoned-decimal character: '" + lastChar + "' in '" + raw + "'");
        }

        BigDecimal value = new BigDecimal(digits);
        if (decimalPlaces > 0) {
            value = value.movePointLeft(decimalPlaces);
        }
        if (negative) {
            value = value.negate();
        }
        return value.setScale(decimalPlaces, RoundingMode.UNNECESSARY);
    }

    /**
     * Formats a BigDecimal back to a COBOL zoned-decimal string with sign overpunch.
     *
     * @param value         the value to format
     * @param totalDigits   total integer + decimal digits (PIC S9(10)V99 = 12)
     * @param decimalPlaces implied decimal places
     * @return the formatted COBOL string
     */
    public static String format(BigDecimal value, int totalDigits, int decimalPlaces) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }

        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().setScale(decimalPlaces, RoundingMode.HALF_UP);
        String plain = abs.movePointRight(decimalPlaces).toBigInteger().toString();

        // Pad with leading zeros
        while (plain.length() < totalDigits) {
            plain = "0" + plain;
        }
        if (plain.length() > totalDigits) {
            plain = plain.substring(plain.length() - totalDigits);
        }

        // Apply sign overpunch to last character
        int lastDigit = plain.charAt(plain.length() - 1) - '0';
        char overpunch = negative ? NEG_OVERPUNCH.charAt(lastDigit) : POS_OVERPUNCH.charAt(lastDigit);
        return plain.substring(0, plain.length() - 1) + overpunch;
    }
}
