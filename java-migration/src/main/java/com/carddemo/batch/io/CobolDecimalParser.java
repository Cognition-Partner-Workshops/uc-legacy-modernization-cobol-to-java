package com.carddemo.batch.io;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Parses COBOL signed display (zoned-decimal) numbers with trailing overpunch.
 *
 * In COBOL PIC S9(n) DISPLAY format the sign is encoded in the last byte
 * using the "trailing overpunch" convention:
 *
 *   Positive:  { = 0, A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9
 *   Negative:  } = 0, J = 1, K = 2, L = 3, M = 4, N = 5, O = 6, P = 7, Q = 8, R = 9
 *
 * This class converts such strings into BigDecimal values, applying an
 * implied decimal point (V in COBOL PIC) at the specified scale.
 */
public final class CobolDecimalParser {

    private CobolDecimalParser() { }

    /**
     * Parse a COBOL signed display number.
     *
     * @param raw           the raw string from the fixed-width record
     * @param impliedScale  number of implied decimal places (V99 → 2)
     * @return the parsed BigDecimal value
     */
    public static BigDecimal parse(String raw, int impliedScale) {
        if (raw == null || raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = trimmed.charAt(trimmed.length() - 1);
        String digits = trimmed.substring(0, trimmed.length() - 1);
        int lastDigit;
        boolean negative;

        if (lastChar >= '0' && lastChar <= '9') {
            // unsigned – treat as positive
            lastDigit = lastChar - '0';
            negative = false;
        } else {
            int[] decoded = decodeOverpunch(lastChar);
            lastDigit = decoded[0];
            negative = decoded[1] != 0;
        }

        String allDigits = digits + lastDigit;
        BigDecimal value = new BigDecimal(allDigits).movePointLeft(impliedScale);
        return negative ? value.negate() : value;
    }

    /**
     * Parse an unsigned COBOL display number (PIC 9(n) without S).
     *
     * @param raw  the raw string from the fixed-width record
     * @return the parsed long value
     */
    public static long parseUnsigned(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        return Long.parseLong(raw.trim());
    }

    /**
     * Decode a trailing overpunch character.
     *
     * @return int[2]: [0] = digit value 0-9, [1] = 0 for positive, 1 for negative
     */
    private static int[] decodeOverpunch(char ch) {
        return switch (ch) {
            case '{' -> new int[]{0, 0};
            case 'A' -> new int[]{1, 0};
            case 'B' -> new int[]{2, 0};
            case 'C' -> new int[]{3, 0};
            case 'D' -> new int[]{4, 0};
            case 'E' -> new int[]{5, 0};
            case 'F' -> new int[]{6, 0};
            case 'G' -> new int[]{7, 0};
            case 'H' -> new int[]{8, 0};
            case 'I' -> new int[]{9, 0};
            case '}' -> new int[]{0, 1};
            case 'J' -> new int[]{1, 1};
            case 'K' -> new int[]{2, 1};
            case 'L' -> new int[]{3, 1};
            case 'M' -> new int[]{4, 1};
            case 'N' -> new int[]{5, 1};
            case 'O' -> new int[]{6, 1};
            case 'P' -> new int[]{7, 1};
            case 'Q' -> new int[]{8, 1};
            case 'R' -> new int[]{9, 1};
            default  -> throw new IllegalArgumentException(
                    "Invalid overpunch character: '" + ch + "' (0x" +
                            Integer.toHexString(ch) + ")");
        };
    }
}
