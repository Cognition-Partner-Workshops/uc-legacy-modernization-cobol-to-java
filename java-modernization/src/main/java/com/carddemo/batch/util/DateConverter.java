package com.carddemo.batch.util;

/**
 * Java equivalent of the COBDATFT assembler routine referenced by CBACT01C.
 * <p>
 * The COBOL program uses copybook CODATECN to pass a date-conversion request:
 * <ul>
 *   <li>Input type '1' = YYYYMMDD</li>
 *   <li>Input type '2' = YYYY-MM-DD</li>
 *   <li>Output type '1' = YYYY-MM-DD</li>
 *   <li>Output type '2' = YYYYMMDD</li>
 * </ul>
 * CBACT01C always calls it with input type '2' and output type '2',
 * converting {@code YYYY-MM-DD} to {@code YYYYMMDD}.
 */
public final class DateConverter {

    private DateConverter() {
    }

    /**
     * Convert a date string between formats.
     *
     * @param inputDate  the date string to convert
     * @param inputType  '1' for YYYYMMDD, '2' for YYYY-MM-DD
     * @param outputType '1' for YYYY-MM-DD, '2' for YYYYMMDD
     * @return the converted date string
     */
    public static String convert(String inputDate, char inputType, char outputType) {
        String yyyy, mm, dd;

        switch (inputType) {
            case '1' -> {
                // YYYYMMDD
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(4, 6);
                dd = inputDate.substring(6, 8);
            }
            case '2' -> {
                // YYYY-MM-DD
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(5, 7);
                dd = inputDate.substring(8, 10);
            }
            default -> throw new IllegalArgumentException("Unknown input type: " + inputType);
        }

        return switch (outputType) {
            case '1' -> yyyy + "-" + mm + "-" + dd; // YYYY-MM-DD
            case '2' -> yyyy + mm + dd;              // YYYYMMDD
            default -> throw new IllegalArgumentException("Unknown output type: " + outputType);
        };
    }
}
