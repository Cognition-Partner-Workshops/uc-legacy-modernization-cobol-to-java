package com.carddemo.batch.util;

/**
 * Replacement for the COBDATFT assembler routine referenced via
 * {@code CALL 'COBDATFT' USING CODATECN-REC} in the COBOL source.
 *
 * Supports the two conversion types used by CBACT01C:
 *   Type "2" input  (YYYY-MM-DD) → Type "2" output (YYYYMMDD, padded to 10 chars)
 *   Type "1" input  (YYYYMMDD)   → Type "1" output (YYYY-MM-DD)
 */
public final class DateConverter {

    private DateConverter() {}

    /**
     * Convert a date between COBOL formats.
     *
     * @param inputDate the date string to convert
     * @param inType    "1" = YYYYMMDD, "2" = YYYY-MM-DD
     * @param outType   "1" = YYYY-MM-DD, "2" = YYYYMMDD
     * @return the reformatted date, right-padded to 10 characters
     */
    public static String convert(String inputDate, String inType, String outType) {
        String yyyy;
        String mm;
        String dd;

        if ("2".equals(inType)) {
            // Input: YYYY-MM-DD
            yyyy = inputDate.substring(0, 4);
            mm   = inputDate.substring(5, 7);
            dd   = inputDate.substring(8, 10);
        } else {
            // Input: YYYYMMDD
            yyyy = inputDate.substring(0, 4);
            mm   = inputDate.substring(4, 6);
            dd   = inputDate.substring(6, 8);
        }

        String result;
        if ("2".equals(outType)) {
            result = yyyy + mm + dd;
        } else {
            result = yyyy + "-" + mm + "-" + dd;
        }

        return String.format("%-10s", result);
    }
}
