package com.cardemo.batch.util;

/**
 * Replaces the COBDATFT assembler program used by CBACT01C for date
 * format conversion. Supports the two conversion modes defined in
 * the CODATECN copybook.
 */
public final class DateConverter {

    private DateConverter() {}

    /**
     * Input format types matching CODATECN-TYPE.
     */
    public enum InputFormat {
        /** YYYYMMDD (type "1") */
        YYYYMMDD,
        /** YYYY-MM-DD (type "2") */
        YYYY_MM_DD
    }

    /**
     * Output format types matching CODATECN-OUTTYPE.
     */
    public enum OutputFormat {
        /** YYYY-MM-DD (type "1") */
        YYYY_MM_DD,
        /** YYYYMMDD (type "2") */
        YYYYMMDD
    }

    /**
     * Convert a date string between COBOL date formats.
     *
     * @param inputDate    the input date string
     * @param inputFormat  the format of the input
     * @param outputFormat the desired output format
     * @return the reformatted date string
     */
    public static String convert(String inputDate, InputFormat inputFormat,
                                 OutputFormat outputFormat) {
        String yyyy, mm, dd;

        switch (inputFormat) {
            case YYYYMMDD -> {
                yyyy = inputDate.substring(0, 4);
                mm   = inputDate.substring(4, 6);
                dd   = inputDate.substring(6, 8);
            }
            case YYYY_MM_DD -> {
                yyyy = inputDate.substring(0, 4);
                mm   = inputDate.substring(5, 7);
                dd   = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unknown input format: " + inputFormat);
        }

        return switch (outputFormat) {
            case YYYY_MM_DD -> yyyy + "-" + mm + "-" + dd;
            case YYYYMMDD   -> yyyy + mm + dd;
        };
    }
}
