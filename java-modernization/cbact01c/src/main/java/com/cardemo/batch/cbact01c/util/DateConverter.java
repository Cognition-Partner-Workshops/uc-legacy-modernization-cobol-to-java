package com.cardemo.batch.cbact01c.util;

/**
 * Replaces the mainframe assembler routine COBDATFT.
 *
 * <p>Supports the two conversion modes used by CBACT01C:
 * <ul>
 *   <li>Type&nbsp;1 (YYYYMMDD)  ↔ Type&nbsp;2 (YYYY-MM-DD)</li>
 * </ul>
 *
 * <p>The COBOL program invokes COBDATFT with input-type = '2' and
 * output-type = '2', meaning <em>YYYY-MM-DD → YYYYMMDD</em>.
 */
public final class DateConverter {

    private DateConverter() {}

    /**
     * Convert a date between the two supported formats.
     *
     * @param inputDate  the date string to convert
     * @param inputType  '1' for YYYYMMDD, '2' for YYYY-MM-DD
     * @param outputType '1' for YYYY-MM-DD, '2' for YYYYMMDD
     * @return the converted date string
     * @throws IllegalArgumentException on invalid type codes or malformed dates
     */
    public static String convert(String inputDate, char inputType, char outputType) {
        String yyyy;
        String mm;
        String dd;

        switch (inputType) {
            case '1' -> { // YYYYMMDD
                if (inputDate.length() < 8) {
                    throw new IllegalArgumentException(
                            "YYYYMMDD input too short: " + inputDate);
                }
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(4, 6);
                dd = inputDate.substring(6, 8);
            }
            case '2' -> { // YYYY-MM-DD
                if (inputDate.length() < 10) {
                    throw new IllegalArgumentException(
                            "YYYY-MM-DD input too short: " + inputDate);
                }
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(5, 7);
                dd = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported input type: " + inputType);
        }

        return switch (outputType) {
            case '1' -> yyyy + "-" + mm + "-" + dd;    // YYYY-MM-DD
            case '2' -> yyyy + mm + dd;                 // YYYYMMDD
            default -> throw new IllegalArgumentException(
                    "Unsupported output type: " + outputType);
        };
    }
}
