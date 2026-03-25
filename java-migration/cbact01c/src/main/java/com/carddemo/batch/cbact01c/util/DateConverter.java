package com.carddemo.batch.cbact01c.util;

/**
 * Java replacement for the COBDATFT assembler program.
 * <p>
 * COBDATFT performs date format conversion controlled by type codes:
 * <pre>
 *   Input type "2" (YYYY-MM-DD) + Output type "2" (YYYYMMDD)
 *   => strips dashes: "2025-05-20" -> "20250520"
 * </pre>
 * <p>
 * In CBACT01C, both CODATECN-TYPE and CODATECN-OUTTYPE are set to "2",
 * so the conversion is always: YYYY-MM-DD -> YYYYMMDD.
 */
public final class DateConverter {

    private DateConverter() {}

    /**
     * Converts a date from YYYY-MM-DD format to YYYYMMDD format.
     * This replicates the COBDATFT assembler behavior when both
     * input type and output type are "2".
     *
     * @param yyyyMmDd the input date in YYYY-MM-DD format
     * @return the date in YYYYMMDD format, padded to 10 chars with spaces
     */
    public static String convertYyyyMmDdToCompact(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.length() < 10) {
            return padRight("", 10);
        }
        // Strip the dashes: "2025-05-20" -> "20250520"
        String compact = yyyyMmDd.substring(0, 4)
                + yyyyMmDd.substring(5, 7)
                + yyyyMmDd.substring(8, 10);
        return padRight(compact, 10);
    }

    /**
     * Extracts the year (first 4 characters) from a YYYY-MM-DD date.
     * This mirrors the COBOL: MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY
     *
     * @param yyyyMmDd the date in YYYY-MM-DD format
     * @return the 4-character year
     */
    public static String extractYear(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.length() < 4) {
            return "    ";
        }
        return yyyyMmDd.substring(0, 4);
    }

    private static String padRight(String s, int length) {
        if (s.length() >= length) {
            return s.substring(0, length);
        }
        return s + " ".repeat(length - s.length());
    }
}
