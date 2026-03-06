package com.carddemo.batch.service;

/**
 * Replaces the COBOL COBDATFT assembler subroutine for date format conversion.
 * <p>
 * Supports the same conversion types defined in the CODATECN.cpy copybook:
 * <ul>
 *   <li>Type 1: YYYYMMDD (compact, no separators)</li>
 *   <li>Type 2: YYYY-MM-DD (ISO-style with dashes)</li>
 * </ul>
 * <p>
 * The COBOL program calls COBDATFT with input type '2' and output type '2',
 * which converts from YYYY-MM-DD to YYYYMMDD.
 */
public final class DateConverter {

    /** Input/output format: YYYYMMDD */
    public static final String TYPE_COMPACT = "1";
    /** Input/output format: YYYY-MM-DD */
    public static final String TYPE_DASHED = "2";

    private DateConverter() {
        // utility class
    }

    /**
     * Converts a date string from one format to another, mirroring the COBDATFT
     * subroutine behaviour used via the CODATECN copybook.
     *
     * @param inputDate  the date string to convert
     * @param inputType  the format of the input ("1" for YYYYMMDD, "2" for YYYY-MM-DD)
     * @param outputType the desired output format ("1" for YYYY-MM-DD, "2" for YYYYMMDD)
     * @return the converted date string
     * @throws IllegalArgumentException if the input type or format is invalid
     */
    public static String convert(String inputDate, String inputType, String outputType) {
        String yyyy;
        String mm;
        String dd;

        switch (inputType) {
            case TYPE_COMPACT -> {
                // Input: YYYYMMDD
                validateLength(inputDate, 8, "YYYYMMDD");
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(4, 6);
                dd = inputDate.substring(6, 8);
            }
            case TYPE_DASHED -> {
                // Input: YYYY-MM-DD
                validateLength(inputDate, 10, "YYYY-MM-DD");
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(5, 7);
                dd = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unknown input type: " + inputType);
        }

        return switch (outputType) {
            case TYPE_DASHED -> yyyy + "-" + mm + "-" + dd;
            case TYPE_COMPACT -> yyyy + mm + dd;
            default -> throw new IllegalArgumentException("Unknown output type: " + outputType);
        };
    }

    private static void validateLength(String date, int expected, String format) {
        if (date == null || date.trim().length() < expected) {
            throw new IllegalArgumentException(
                    "Invalid date for format " + format + ": '" + date + "'");
        }
    }
}
