package com.carddemo.batch.cbact01c.service;

/**
 * Java equivalent of the COBOL COBDATFT assembler routine used with the
 * CODATECN copybook. Converts dates between formats.
 *
 * <p>In CBACT01C, the routine is called with:
 * <ul>
 *   <li>CODATECN-TYPE = '2' (input is YYYY-MM-DD)</li>
 *   <li>CODATECN-OUTTYPE = '2' (output is YYYYMMDD)</li>
 * </ul>
 *
 * <p>IMPORTANT: The COBOL copybook CODATECN defines <b>different</b> mappings
 * for input vs. output type codes:
 * <ul>
 *   <li>Input:  '1' = YYYYMMDD, '2' = YYYY-MM-DD</li>
 *   <li>Output: '1' = YYYY-MM-DD, '2' = YYYYMMDD (inverted!)</li>
 * </ul>
 */
public final class DateConverter {

    // --- Input type constants (CODATECN-TYPE) ---
    /** Input format type '1': YYYYMMDD (compact, no separators). */
    public static final char INPUT_YYYYMMDD = '1';
    /** Input format type '2': YYYY-MM-DD (ISO-style with hyphens). */
    public static final char INPUT_YYYY_MM_DD = '2';

    // --- Output type constants (CODATECN-OUTTYPE) ---
    /** Output format type '1': YYYY-MM-DD (with hyphens). */
    public static final char OUTPUT_YYYY_MM_DD = '1';
    /** Output format type '2': YYYYMMDD (compact, no separators). */
    public static final char OUTPUT_YYYYMMDD = '2';

    private DateConverter() {
        // utility class
    }

    /**
     * Converts a date string from one format to another, replicating the
     * COBDATFT assembler routine behaviour.
     *
     * @param inputDate  the date string to convert
     * @param inputType  '1' for YYYYMMDD input, '2' for YYYY-MM-DD input
     * @param outputType '1' for YYYY-MM-DD output, '2' for YYYYMMDD output
     * @return the converted date string
     * @throws IllegalArgumentException if the format types are unsupported
     */
    public static String convert(String inputDate, char inputType, char outputType) {
        String yyyy;
        String mm;
        String dd;

        switch (inputType) {
            case INPUT_YYYYMMDD -> {
                // Input: YYYYMMDD
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(4, 6);
                dd = inputDate.substring(6, 8);
            }
            case INPUT_YYYY_MM_DD -> {
                // Input: YYYY-MM-DD
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(5, 7);
                dd = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported input date format type: " + inputType);
        }

        return switch (outputType) {
            case OUTPUT_YYYYMMDD -> yyyy + mm + dd;
            case OUTPUT_YYYY_MM_DD -> yyyy + "-" + mm + "-" + dd;
            default -> throw new IllegalArgumentException(
                    "Unsupported output date format type: " + outputType);
        };
    }

    /**
     * Extracts the four-digit year from a YYYY-MM-DD date string.
     *
     * @param dateYyyyMmDd date in YYYY-MM-DD format
     * @return the year portion (first 4 characters)
     */
    public static String extractYear(String dateYyyyMmDd) {
        return dateYyyyMmDd.substring(0, 4);
    }
}
