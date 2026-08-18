package com.carddemo.statement;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Decoding of COBOL zoned-decimal (DISPLAY) numeric fields such as
 * {@code PIC S9(09)V99}, where the sign is overpunched onto the last digit.
 *
 * <p>Both sign conventions found in the CardDemo data are supported: the
 * mainframe overpunch ({@code '{'} = +0, {@code 'A'..'I'} = +1..+9,
 * {@code '}'} = -0, {@code 'J'..'R'} = -1..-9) and the native ASCII
 * convention GnuCOBOL uses ({@code 'p'..'y'} = -0..-9). Plain digits are
 * positive; a leading or trailing {@code '+'}/{@code '-'} is also accepted.
 */
public final class ZonedDecimal {

    private ZonedDecimal() {
    }

    /** Decodes {@code raw} as a zoned-decimal value with {@code scale} implied decimals. */
    public static BigDecimal decode(String raw, int scale) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale);
        }
        boolean negative = false;
        if (value.charAt(0) == '+' || value.charAt(0) == '-') {
            negative = value.charAt(0) == '-';
            value = value.substring(1);
        }
        StringBuilder digits = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean last = i == value.length() - 1;
            if (c >= '0' && c <= '9') {
                digits.append(c);
            } else if (c == ' ') {
                digits.append('0');
            } else if (last && (c == '+' || c == '-')) {
                negative = c == '-';
            } else if (last) {
                negative |= isNegativeOverpunch(c);
                digits.append(overpunchDigit(c, raw));
            } else {
                throw new IllegalArgumentException("Not a zoned-decimal field: '" + raw + "'");
            }
        }
        BigInteger unscaled = new BigInteger(digits.toString());
        if (negative) {
            unscaled = unscaled.negate();
        }
        return new BigDecimal(unscaled, scale);
    }

    private static boolean isNegativeOverpunch(char c) {
        return c == '}' || (c >= 'J' && c <= 'R') || (c >= 'p' && c <= 'y');
    }

    private static char overpunchDigit(char c, String raw) {
        if (c == '{' || c == '}') {
            return '0';
        }
        if (c >= 'A' && c <= 'I') {
            return (char) ('1' + (c - 'A'));
        }
        if (c >= 'J' && c <= 'R') {
            return (char) ('1' + (c - 'J'));
        }
        if (c >= 'p' && c <= 'y') {
            return (char) ('0' + (c - 'p'));
        }
        throw new IllegalArgumentException("Unsupported overpunch character '" + c
                + "' in zoned-decimal field '" + raw + "'");
    }
}
