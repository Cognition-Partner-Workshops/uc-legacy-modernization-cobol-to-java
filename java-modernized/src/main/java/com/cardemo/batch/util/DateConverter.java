package com.cardemo.batch.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Replaces the COBDATFT assembler routine used by CBACT01C for date formatting.
 *
 * <p>COBDATFT converted between two date formats controlled by CODATECN-TYPE
 * and CODATECN-OUTTYPE:
 * <ul>
 *   <li>Type "1" = YYYYMMDD (compact)</li>
 *   <li>Type "2" = YYYY-MM-DD (ISO-8601 with dashes)</li>
 * </ul>
 *
 * <p>In CBACT01C, the call is always: input type "2" → output type "2" (YYYYMMDD).
 * However, this utility supports all four conversion combinations for completeness.
 */
public final class DateConverter {

    private static final DateTimeFormatter ISO_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private DateConverter() {
    }

    /**
     * Converts a date string between COBOL date formats.
     *
     * @param inputDate  the date string to convert
     * @param inputType  "1" for YYYYMMDD, "2" for YYYY-MM-DD
     * @param outputType "1" for YYYY-MM-DD output, "2" for YYYYMMDD output
     * @return the reformatted date string, or the original input if parsing fails
     */
    public static String convert(String inputDate, String inputType, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return inputDate;
        }

        try {
            LocalDate date = parseInput(inputDate.trim(), inputType);
            return formatOutput(date, outputType);
        } catch (DateTimeParseException e) {
            return inputDate;
        }
    }

    /**
     * Convenience method matching the exact CBACT01C usage:
     * YYYY-MM-DD → YYYYMMDD.
     */
    public static String isoToCompact(String isoDate) {
        return convert(isoDate, "2", "2");
    }

    private static LocalDate parseInput(String input, String type) {
        return switch (type) {
            case "1" -> LocalDate.parse(input, COMPACT);
            case "2" -> LocalDate.parse(input, ISO_DASH);
            default -> throw new IllegalArgumentException("Unknown input type: " + type);
        };
    }

    private static String formatOutput(LocalDate date, String type) {
        return switch (type) {
            case "1" -> date.format(ISO_DASH);
            case "2" -> date.format(COMPACT);
            default -> throw new IllegalArgumentException("Unknown output type: " + type);
        };
    }
}
