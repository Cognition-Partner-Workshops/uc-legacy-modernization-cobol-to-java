package com.carddemo.batch.io;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Handles ASCII zoned-decimal encoding used in COBOL DISPLAY fields.
 * The trailing byte carries an overpunch sign character.
 *
 * Positive: {=0  A=1  B=2  C=3  D=4  E=5  F=6  G=7  H=8  I=9
 * Negative: }=0  J=1  K=2  L=3  M=4  N=5  O=6  P=7  Q=8  R=9
 */
public final class ZonedDecimalUtil {

    private static final String POS_CHARS = "{ABCDEFGHI";
    private static final String NEG_CHARS = "}JKLMNOPQR";

    private ZonedDecimalUtil() {}

    /**
     * Parse a zoned-decimal string (with trailing overpunch) into a BigDecimal.
     *
     * @param raw           the raw zoned-decimal text (e.g. "00000001940{")
     * @param decimalPlaces number of implied decimal places (V99 → 2)
     * @return the parsed value
     */
    public static BigDecimal parse(String raw, int decimalPlaces) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        boolean negative = false;
        int lastDigit;

        int posIdx = POS_CHARS.indexOf(lastChar);
        if (posIdx >= 0) {
            lastDigit = posIdx;
        } else {
            int negIdx = NEG_CHARS.indexOf(lastChar);
            if (negIdx >= 0) {
                lastDigit = negIdx;
                negative = true;
            } else if (Character.isDigit(lastChar)) {
                lastDigit = lastChar - '0';
            } else {
                throw new IllegalArgumentException(
                        "Invalid zoned-decimal trailing character: '" + lastChar + "' in \"" + raw + "\"");
            }
        }

        String allDigits = digits + lastDigit;
        BigDecimal value = new BigDecimal(allDigits)
                .movePointLeft(decimalPlaces);

        return negative ? value.negate() : value;
    }

    /**
     * Format a BigDecimal into a zoned-decimal string with trailing overpunch.
     *
     * @param value         the numeric value
     * @param totalDigits   total number of digits (integer + decimal)
     * @param decimalPlaces number of implied decimal places
     * @return the formatted zoned-decimal string
     */
    public static String format(BigDecimal value, int totalDigits, int decimalPlaces) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs().setScale(decimalPlaces, RoundingMode.HALF_UP);
        String unscaled = abs.movePointRight(decimalPlaces)
                .toBigInteger()
                .toString();

        while (unscaled.length() < totalDigits) {
            unscaled = "0" + unscaled;
        }
        if (unscaled.length() > totalDigits) {
            unscaled = unscaled.substring(unscaled.length() - totalDigits);
        }

        String leadDigits = unscaled.substring(0, unscaled.length() - 1);
        int lastDigit = unscaled.charAt(unscaled.length() - 1) - '0';
        char overpunch = negative ? NEG_CHARS.charAt(lastDigit) : POS_CHARS.charAt(lastDigit);

        return leadDigits + overpunch;
    }
}
