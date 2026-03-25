package com.carddemo.util;

/**
 * Pure-Java replacement for the assembler program COBDATFT.
 *
 * Supports the same input/output type codes as the COBOL copybook CODATECN:
 * <pre>
 *   Type 1  =  YYYYMMDD      (no separators)
 *   Type 2  =  YYYY-MM-DD    (ISO-8601 with dashes)
 * </pre>
 *
 * Conversion rules (matching the assembler):
 * <ul>
 *   <li>Type&nbsp;1 &rarr; Type&nbsp;1 &mdash; YYYY-MM-DD output (adds dashes)</li>
 *   <li>Type&nbsp;2 &rarr; Type&nbsp;2 &mdash; YYYYMMDD output   (strips dashes)</li>
 *   <li>Same-to-same or cross-type with invalid combo &rarr; error</li>
 * </ul>
 */
public final class DateFormatter {

    private DateFormatter() {}

    /**
     * Converts a date string between COBOL date formats.
     *
     * @param inputDate  the input date string
     * @param inputType  '1' for YYYYMMDD, '2' for YYYY-MM-DD
     * @param outputType '1' for YYYY-MM-DD, '2' for YYYYMMDD
     * @return the converted date string
     * @throws IllegalArgumentException if the input/output type combination is invalid
     */
    public static String convert(String inputDate, char inputType, char outputType) {
        return switch (inputType) {
            case '1' -> {
                if (outputType == '2') {
                    throw new IllegalArgumentException("INVALID INPUT: type 1->2 not supported");
                }
                // YYYYMMDD -> YYYY-MM-DD
                String yyyy = inputDate.substring(0, 4);
                String mm   = inputDate.substring(4, 6);
                String dd   = inputDate.substring(6, 8);
                yield yyyy + "-" + mm + "-" + dd;
            }
            case '2' -> {
                if (outputType == '1') {
                    throw new IllegalArgumentException("INVALID INPUT: type 2->1 not supported");
                }
                // YYYY-MM-DD -> YYYYMMDD
                String yyyy = inputDate.substring(0, 4);
                String mm   = inputDate.substring(5, 7);
                String dd   = inputDate.substring(8, 10);
                yield yyyy + mm + dd;
            }
            default -> throw new IllegalArgumentException("INVALID INPUT: unknown input type '" + inputType + "'");
        };
    }
}
