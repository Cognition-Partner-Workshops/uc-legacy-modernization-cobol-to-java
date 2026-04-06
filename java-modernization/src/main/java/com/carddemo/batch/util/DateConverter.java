package com.carddemo.batch.util;

/**
 * Replaces the COBOL assembler subroutine COBDATFT for date-format conversion.
 * <p>
 * Supported conversions (matching CODATECN copybook types):
 * <ul>
 *   <li>Type 1 input  = YYYYMMDD</li>
 *   <li>Type 2 input  = YYYY-MM-DD</li>
 *   <li>Type 1 output = YYYY-MM-DD</li>
 *   <li>Type 2 output = YYYYMMDD</li>
 * </ul>
 */
public final class DateConverter {

    /** Input is YYYYMMDD (no separators). */
    public static final int TYPE_YYYYMMDD = 1;
    /** Input/output is YYYY-MM-DD (with dashes). */
    public static final int TYPE_YYYY_MM_DD = 2;

    private DateConverter() {
        // utility class
    }

    /**
     * Converts a date string between COBOL date formats.
     *
     * @param inputDate  the source date string (trimmed of trailing spaces)
     * @param inType     input format type (1 = YYYYMMDD, 2 = YYYY-MM-DD)
     * @param outType    output format type (1 = YYYY-MM-DD, 2 = YYYYMMDD)
     * @return the converted date string, right-padded with spaces to 10 characters
     *         to match COBOL PIC X(10) semantics
     * @throws IllegalArgumentException if the input type or date is invalid
     */
    public static String convert(String inputDate, int inType, int outType) {
        if (inputDate == null || inputDate.isBlank()) {
            throw new IllegalArgumentException("Input date must not be null or blank");
        }

        String trimmed = inputDate.trim();

        // Normalise to components
        String yyyy, mm, dd;
        switch (inType) {
            case TYPE_YYYYMMDD -> {
                if (trimmed.length() < 8) {
                    throw new IllegalArgumentException("YYYYMMDD input too short: " + trimmed);
                }
                yyyy = trimmed.substring(0, 4);
                mm = trimmed.substring(4, 6);
                dd = trimmed.substring(6, 8);
            }
            case TYPE_YYYY_MM_DD -> {
                if (trimmed.length() < 10 || trimmed.charAt(4) != '-' || trimmed.charAt(7) != '-') {
                    throw new IllegalArgumentException("YYYY-MM-DD input invalid: " + trimmed);
                }
                yyyy = trimmed.substring(0, 4);
                mm = trimmed.substring(5, 7);
                dd = trimmed.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unknown input type: " + inType);
        }

        // Format output
        String result = switch (outType) {
            case TYPE_YYYYMMDD -> yyyy + mm + dd;          // 8 chars
            case TYPE_YYYY_MM_DD -> yyyy + "-" + mm + "-" + dd; // 10 chars
            default -> throw new IllegalArgumentException("Unknown output type: " + outType);
        };

        // Pad to 10 characters to match COBOL PIC X(10)
        return padRight(result, 10);
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) {
            return s.substring(0, width);
        }
        return s + " ".repeat(width - s.length());
    }
}
