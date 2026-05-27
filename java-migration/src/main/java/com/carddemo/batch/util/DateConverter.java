package com.carddemo.batch.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Replaces the COBDATFT assembler subroutine referenced via the CODATECN copybook.
 * <p>
 * Supported conversions (mirroring CODATECN-TYPE / CODATECN-OUTTYPE):
 * <ul>
 *   <li>Type 1 = YYYYMMDD</li>
 *   <li>Type 2 = YYYY-MM-DD</li>
 * </ul>
 */
public final class DateConverter {

    private static final DateTimeFormatter COMPACT  = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateConverter() {}

    /**
     * Converts a date string between the two supported formats.
     *
     * @param inputDate  the date string to convert
     * @param inType     input format: "1" for YYYYMMDD, "2" for YYYY-MM-DD
     * @param outType    output format: "1" for YYYY-MM-DD, "2" for YYYYMMDD
     * @return the converted date string
     * @throws DateTimeParseException if the input cannot be parsed
     * @throws IllegalArgumentException if an unsupported type is specified
     */
    public static String convert(String inputDate, String inType, String outType) {
        LocalDate date = switch (inType) {
            case "1" -> LocalDate.parse(inputDate.trim(), COMPACT);
            case "2" -> LocalDate.parse(inputDate.trim(), ISO_DATE);
            default  -> throw new IllegalArgumentException("Unsupported input type: " + inType);
        };
        return switch (outType) {
            case "1" -> date.format(ISO_DATE);
            case "2" -> date.format(COMPACT);
            default  -> throw new IllegalArgumentException("Unsupported output type: " + outType);
        };
    }
}
