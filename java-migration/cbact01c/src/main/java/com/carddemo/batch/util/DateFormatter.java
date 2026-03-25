package com.carddemo.batch.util;

/**
 * Replaces the COBDATFT assembler program for date format conversion.
 *
 * <p>The original assembler supports two conversions:
 * <ul>
 *   <li>Type 1 input (YYYYMMDD) → Type 1 output (YYYY-MM-DD)</li>
 *   <li>Type 2 input (YYYY-MM-DD) → Type 2 output (YYYYMMDD)</li>
 * </ul>
 *
 * <p>CBACT01C uses Type 2→2: strips dashes from YYYY-MM-DD to produce YYYYMMDD.
 */
public final class DateFormatter {

    private DateFormatter() {}

    /**
     * Convert YYYYMMDD to YYYY-MM-DD (Type 1 → Type 1 in COBDATFT).
     *
     * @param yyyymmdd 8-character date string
     * @return formatted date with dashes
     * @throws IllegalArgumentException if input is not 8 characters
     */
    public static String toHyphenated(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() < 8) {
            throw new IllegalArgumentException(
                    "INVALID INPUT: expected YYYYMMDD, got: " + yyyymmdd);
        }
        return yyyymmdd.substring(0, 4) + "-"
                + yyyymmdd.substring(4, 6) + "-"
                + yyyymmdd.substring(6, 8);
    }

    /**
     * Convert YYYY-MM-DD to YYYYMMDD (Type 2 → Type 2 in COBDATFT).
     *
     * <p>This is the conversion used by CBACT01C for the reissue date.
     *
     * @param hyphenated 10-character date string with dashes
     * @return compact date without dashes
     * @throws IllegalArgumentException if input is not 10 characters
     */
    public static String toCompact(String hyphenated) {
        if (hyphenated == null || hyphenated.length() < 10) {
            throw new IllegalArgumentException(
                    "INVALID INPUT: expected YYYY-MM-DD, got: " + hyphenated);
        }
        // MVC COOUTDT(4),COINPDT       → chars 0-3 (YYYY)
        // MVC COOUTDT+4(2),COINPDT+5   → chars 5-6 (MM)
        // MVC COOUTDT+6(2),COINPDT+8   → chars 8-9 (DD)
        return hyphenated.substring(0, 4)
                + hyphenated.substring(5, 7)
                + hyphenated.substring(8, 10);
    }
}
