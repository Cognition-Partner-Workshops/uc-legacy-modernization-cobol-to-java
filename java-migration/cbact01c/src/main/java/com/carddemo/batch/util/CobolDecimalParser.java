package com.carddemo.batch.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Parses COBOL signed decimal display fields with overpunch encoding.
 *
 * In COBOL DISPLAY format, PIC S9(n)V99 stores the sign in the last byte
 * using "overpunch" characters:
 * <ul>
 *   <li>Positive: '{' (0), 'A'-'I' (1-9)</li>
 *   <li>Negative: '}' (0), 'J'-'R' (1-9)</li>
 * </ul>
 *
 * The 'V' represents an implied decimal point — no actual decimal character
 * is stored in the data.
 */
public final class CobolDecimalParser {

    private CobolDecimalParser() {
    }

    private static final Map<Character, int[]> OVERPUNCH_MAP = Map.ofEntries(
            // Positive: char → [digit, +1]
            Map.entry('{', new int[]{0, 1}),
            Map.entry('A', new int[]{1, 1}),
            Map.entry('B', new int[]{2, 1}),
            Map.entry('C', new int[]{3, 1}),
            Map.entry('D', new int[]{4, 1}),
            Map.entry('E', new int[]{5, 1}),
            Map.entry('F', new int[]{6, 1}),
            Map.entry('G', new int[]{7, 1}),
            Map.entry('H', new int[]{8, 1}),
            Map.entry('I', new int[]{9, 1}),
            // Negative: char → [digit, -1]
            Map.entry('}', new int[]{0, -1}),
            Map.entry('J', new int[]{1, -1}),
            Map.entry('K', new int[]{2, -1}),
            Map.entry('L', new int[]{3, -1}),
            Map.entry('M', new int[]{4, -1}),
            Map.entry('N', new int[]{5, -1}),
            Map.entry('O', new int[]{6, -1}),
            Map.entry('P', new int[]{7, -1}),
            Map.entry('Q', new int[]{8, -1}),
            Map.entry('R', new int[]{9, -1})
    );

    /**
     * Parse a COBOL signed decimal display field.
     *
     * @param raw           the raw fixed-width string (e.g., "00000001940{")
     * @param decimalPlaces number of implied decimal places (e.g., 2 for V99)
     * @return parsed BigDecimal value
     */
    public static BigDecimal parseSignedDecimal(String raw, int decimalPlaces) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }
        raw = raw.strip();
        if (raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String prefix = raw.substring(0, raw.length() - 1);
        int sign = 1;
        String digitStr;

        int[] overpunch = OVERPUNCH_MAP.get(lastChar);
        if (overpunch != null) {
            digitStr = prefix + overpunch[0];
            sign = overpunch[1];
        } else if (Character.isDigit(lastChar)) {
            digitStr = raw;
        } else {
            return BigDecimal.ZERO;
        }

        BigDecimal value = new BigDecimal(digitStr);
        if (decimalPlaces > 0) {
            value = value.movePointLeft(decimalPlaces);
        }
        if (sign < 0) {
            value = value.negate();
        }
        return value.setScale(decimalPlaces, RoundingMode.HALF_UP);
    }

    /**
     * Parse an unsigned numeric field (PIC 9(n)).
     *
     * @param raw the raw fixed-width string
     * @return parsed long value
     */
    public static long parseUnsignedNumeric(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.strip());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
