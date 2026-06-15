package com.carddemo.io;

/**
 * Java equivalent of the COBDATFT assembler routine called via the CODATECN copybook.
 * Converts dates between formats:
 * <ul>
 *   <li>Type 1 input: YYYYMMDD → Type 1 output: YYYY-MM-DD</li>
 *   <li>Type 2 input: YYYY-MM-DD → Type 2 output: YYYYMMDD</li>
 * </ul>
 *
 * <p>In CBACT01C, the program sets both CODATECN-TYPE='2' and CODATECN-OUTTYPE='2',
 * meaning: input is YYYY-MM-DD, output is YYYYMMDD (strips hyphens).
 */
public final class DateConverter {

    private DateConverter() {}

    /**
     * Converts a date from YYYY-MM-DD format to YYYYMMDD format.
     * Equivalent to COBDATFT with type=2, outtype=2.
     */
    public static String convertYyyyMmDdToCompact(String date) {
        if (date == null || date.length() < 10) {
            return date;
        }
        return date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 10);
    }

    /**
     * Converts a date from YYYYMMDD format to YYYY-MM-DD format.
     * Equivalent to COBDATFT with type=1, outtype=1.
     */
    public static String convertCompactToYyyyMmDd(String date) {
        if (date == null || date.length() < 8) {
            return date;
        }
        return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
    }

    /**
     * Extracts the year (YYYY) from a YYYY-MM-DD formatted date.
     */
    public static String extractYear(String date) {
        if (date == null || date.length() < 4) {
            return "";
        }
        return date.substring(0, 4);
    }
}
