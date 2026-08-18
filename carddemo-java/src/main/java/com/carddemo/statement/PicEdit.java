package com.carddemo.statement;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/** COBOL numeric-edited MOVE targets used by the statement lines. */
public final class PicEdit {

    private static final int INT_DIGITS = 9;
    private static final BigInteger MODULUS = BigInteger.TEN.pow(INT_DIGITS + 2);

    private PicEdit() {
    }

    /**
     * {@code PIC 9(9).99-}: nine zero-filled integer positions, a decimal point, two
     * decimals and a trailing sign position ({@code '-'} when negative, else a space).
     */
    public static String zeroFilled(BigDecimal value) {
        return edit(value, false);
    }

    /**
     * {@code PIC Z(9).99-}: as {@link #zeroFilled(BigDecimal)} but with leading zero
     * suppression over the nine integer positions (suppressed positions become spaces).
     */
    public static String zeroSuppressed(BigDecimal value) {
        return edit(value, true);
    }

    private static String edit(BigDecimal value, boolean suppress) {
        BigDecimal truncated = value.setScale(2, RoundingMode.DOWN);
        boolean negative = truncated.signum() < 0;
        BigInteger cents = truncated.unscaledValue().abs().mod(MODULUS);
        String all = String.format("%0" + (INT_DIGITS + 2) + "d", cents);
        String intPart = all.substring(0, INT_DIGITS);
        String decPart = all.substring(INT_DIGITS);
        if (suppress) {
            int zeros = 0;
            while (zeros < INT_DIGITS && intPart.charAt(zeros) == '0') {
                zeros++;
            }
            intPart = " ".repeat(zeros) + intPart.substring(zeros);
        }
        return intPart + "." + decPart + (negative ? "-" : " ");
    }
}
