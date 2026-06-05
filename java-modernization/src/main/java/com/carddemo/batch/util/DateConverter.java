package com.carddemo.batch.util;

/**
 * Pure-Java replacement for the COBDATFT assembler subroutine referenced by
 * CBACT01C.  The COBOL program passes a {@code CODATECN-REC} structure that
 * specifies an input type, an input date string, an output type, and receives
 * the converted date string.
 *
 * <p>Supported conversions (matching the COBOL copybook values):</p>
 * <ul>
 *   <li>Type {@code 1} — {@code YYYYMMDD}</li>
 *   <li>Type {@code 2} — {@code YYYY-MM-DD}</li>
 * </ul>
 */
public final class DateConverter {

    private DateConverter() {}

    /**
     * Convert a date string between the two supported formats.
     *
     * @param inputDate  the date string to convert
     * @param inputType  {@code "1"} for YYYYMMDD, {@code "2"} for YYYY-MM-DD
     * @param outputType {@code "1"} for YYYY-MM-DD, {@code "2"} for YYYYMMDD
     * @return the reformatted date string
     */
    public static String convert(String inputDate, String inputType, String outputType) {
        String yyyy;
        String mm;
        String dd;

        switch (inputType) {
            case "1" -> {
                // YYYYMMDD
                yyyy = inputDate.substring(0, 4);
                mm   = inputDate.substring(4, 6);
                dd   = inputDate.substring(6, 8);
            }
            case "2" -> {
                // YYYY-MM-DD
                yyyy = inputDate.substring(0, 4);
                mm   = inputDate.substring(5, 7);
                dd   = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported input type: " + inputType);
        }

        return switch (outputType) {
            case "1" -> yyyy + "-" + mm + "-" + dd;   // → YYYY-MM-DD
            case "2" -> yyyy + mm + dd;                // → YYYYMMDD
            default  -> throw new IllegalArgumentException(
                    "Unsupported output type: " + outputType);
        };
    }
}
