package com.carddemo.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

/**
 * Parses COBOL zoned-decimal fields with trailing overpunch sign encoding.
 *
 * In COBOL DISPLAY format, the sign of a numeric field is encoded in the
 * zone nibble of the last byte. In ASCII representation:
 *   Positive: { = 0, A = 1, B = 2, ... I = 9
 *   Negative: } = 0, J = 1, K = 2, ... R = 9
 *
 * This replaces the mainframe-specific EBCDIC zone-nibble encoding with
 * the ASCII overpunch convention used in the CardDemo data files.
 */
public final class ZonedDecimalParser {

    private static final Map<Character, Character> POSITIVE_OVERPUNCH = Map.ofEntries(
            Map.entry('{', '0'), Map.entry('A', '1'), Map.entry('B', '2'),
            Map.entry('C', '3'), Map.entry('D', '4'), Map.entry('E', '5'),
            Map.entry('F', '6'), Map.entry('G', '7'), Map.entry('H', '8'),
            Map.entry('I', '9')
    );

    private static final Map<Character, Character> NEGATIVE_OVERPUNCH = Map.ofEntries(
            Map.entry('}', '0'), Map.entry('J', '1'), Map.entry('K', '2'),
            Map.entry('L', '3'), Map.entry('M', '4'), Map.entry('N', '5'),
            Map.entry('O', '6'), Map.entry('P', '7'), Map.entry('Q', '8'),
            Map.entry('R', '9')
    );

    private ZonedDecimalParser() {
    }

    /**
     * Decode a zoned-decimal field with trailing overpunch sign.
     *
     * @param raw           the raw fixed-width string from the data file
     * @param decimalPlaces number of implied decimal places (V99 = 2)
     * @return the decoded BigDecimal value
     */
    public static BigDecimal decode(String raw, int decimalPlaces) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }

        char lastChar = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        boolean negative = false;

        if (POSITIVE_OVERPUNCH.containsKey(lastChar)) {
            digits += POSITIVE_OVERPUNCH.get(lastChar);
        } else if (NEGATIVE_OVERPUNCH.containsKey(lastChar)) {
            digits += NEGATIVE_OVERPUNCH.get(lastChar);
            negative = true;
        } else if (Character.isDigit(lastChar)) {
            digits += lastChar;
        } else {
            digits += '0';
        }

        BigDecimal value = new BigDecimal(digits)
                .movePointLeft(decimalPlaces);

        return negative ? value.negate() : value;
    }

    /**
     * Decode a standard S9(n)V99 field (2 implied decimal places).
     */
    public static BigDecimal decode(String raw) {
        return decode(raw, 2);
    }
}
