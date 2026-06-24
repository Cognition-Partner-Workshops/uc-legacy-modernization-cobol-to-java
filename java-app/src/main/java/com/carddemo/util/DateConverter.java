package com.carddemo.util;

/**
 * Pure-Java replacement for the assembler program COBDATFT, which converts
 * between date formats using the CODATECN copybook interface.
 *
 * Supported conversions (matching CODATECN-TYPE / CODATECN-OUTTYPE):
 *   Type 1 = YYYYMMDD
 *   Type 2 = YYYY-MM-DD
 */
public final class DateConverter {

    private DateConverter() { }

    public static final int YYYYMMDD = 1;
    public static final int YYYY_MM_DD = 2;

    /**
     * Convert a date string between the two supported formats.
     *
     * @param inputDate  the source date string
     * @param inputType  1 for YYYYMMDD, 2 for YYYY-MM-DD
     * @param outputType 1 for YYYY-MM-DD, 2 for YYYYMMDD
     * @return the reformatted date string
     * @throws IllegalArgumentException on invalid type codes or unparseable input
     */
    public static String convert(String inputDate, int inputType, int outputType) {
        String yyyy;
        String mm;
        String dd;

        switch (inputType) {
            case YYYYMMDD -> {
                if (inputDate.length() < 8) {
                    throw new IllegalArgumentException(
                            "YYYYMMDD input must be at least 8 characters: " + inputDate);
                }
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(4, 6);
                dd = inputDate.substring(6, 8);
            }
            case YYYY_MM_DD -> {
                if (inputDate.length() < 10) {
                    throw new IllegalArgumentException(
                            "YYYY-MM-DD input must be at least 10 characters: " + inputDate);
                }
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(5, 7);
                dd = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unknown input type: " + inputType);
        }

        return switch (outputType) {
            case YYYYMMDD -> yyyy + "-" + mm + "-" + dd;
            case YYYY_MM_DD -> yyyy + mm + dd;
            default -> throw new IllegalArgumentException("Unknown output type: " + outputType);
        };
    }
}
