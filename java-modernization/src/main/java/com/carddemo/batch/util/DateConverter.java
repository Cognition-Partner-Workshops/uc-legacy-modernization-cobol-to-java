package com.carddemo.batch.util;

/**
 * Replaces the COBOL assembler subroutine COBDATFT referenced via
 * copybook CODATECN.cpy.
 * <p>
 * Supported conversions (matching CODATECN-TYPE / CODATECN-OUTTYPE values):
 * <ul>
 *   <li>Type 1 input  → YYYYMMDD</li>
 *   <li>Type 2 input  → YYYY-MM-DD</li>
 *   <li>Type 1 output → YYYY-MM-DD</li>
 *   <li>Type 2 output → YYYYMMDD</li>
 * </ul>
 * CBACT01C uses input type 2 (YYYY-MM-DD) with output type 2 (YYYYMMDD).
 */
public final class DateConverter {

    /** Input format: YYYYMMDD */
    public static final String TYPE_YYYYMMDD     = "1";
    /** Input format: YYYY-MM-DD */
    public static final String TYPE_YYYY_MM_DD   = "2";

    private DateConverter() { /* utility */ }

    /**
     * Converts a date string between COBOL CODATECN formats.
     *
     * @param inputDate the date string to convert
     * @param inType    input type ("1" = YYYYMMDD, "2" = YYYY-MM-DD)
     * @param outType   output type ("1" = YYYY-MM-DD, "2" = YYYYMMDD)
     * @return the reformatted date string
     * @throws IllegalArgumentException if the types are unsupported or input is malformed
     */
    public static String convert(String inputDate, String inType, String outType) {
        String yyyy;
        String mm;
        String dd;

        // Parse input
        switch (inType) {
            case TYPE_YYYYMMDD -> {
                // YYYYMMDD — 8 chars
                if (inputDate.length() < 8) {
                    throw new IllegalArgumentException(
                            "YYYYMMDD input must be at least 8 characters: " + inputDate);
                }
                yyyy = inputDate.substring(0, 4);
                mm   = inputDate.substring(4, 6);
                dd   = inputDate.substring(6, 8);
            }
            case TYPE_YYYY_MM_DD -> {
                // YYYY-MM-DD — 10 chars
                if (inputDate.length() < 10) {
                    throw new IllegalArgumentException(
                            "YYYY-MM-DD input must be at least 10 characters: " + inputDate);
                }
                yyyy = inputDate.substring(0, 4);
                mm   = inputDate.substring(5, 7);
                dd   = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unsupported input type: " + inType);
        }

        // Format output
        return switch (outType) {
            case TYPE_YYYYMMDD     -> yyyy + "-" + mm + "-" + dd;
            case TYPE_YYYY_MM_DD   -> yyyy + mm + dd;
            default -> throw new IllegalArgumentException("Unsupported output type: " + outType);
        };
    }

    /**
     * Extracts the 4-digit year from a YYYY-MM-DD date string.
     * Mirrors COBOL: {@code MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY}.
     *
     * @param yyyyMmDd a date in YYYY-MM-DD format
     * @return the 4-character year string
     */
    public static String extractYear(String yyyyMmDd) {
        if (yyyyMmDd == null || yyyyMmDd.length() < 4) {
            return "    ";
        }
        return yyyyMmDd.substring(0, 4);
    }
}
