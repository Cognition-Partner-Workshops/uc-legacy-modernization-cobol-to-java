package com.cardemo.batch.util;

/**
 * Replaces the COBOL assembler program COBDATFT used via CODATECN copybook.
 * Converts between date formats:
 * <ul>
 *   <li>Type "1" input: YYYYMMDD</li>
 *   <li>Type "2" input: YYYY-MM-DD</li>
 *   <li>OutType "1" output: YYYY-MM-DD</li>
 *   <li>OutType "2" output: YYYYMMDD</li>
 * </ul>
 */
public final class DateConverter {

    private DateConverter() {}

    public static String convert(String inputDate, String inputType, String outputType) {
        if (inputDate == null || inputDate.isBlank()) {
            return "";
        }

        String yyyy;
        String mm;
        String dd;

        switch (inputType) {
            case "1" -> {
                // YYYYMMDD
                String trimmed = inputDate.trim();
                if (trimmed.length() < 8) {
                    return inputDate;
                }
                yyyy = trimmed.substring(0, 4);
                mm = trimmed.substring(4, 6);
                dd = trimmed.substring(6, 8);
            }
            case "2" -> {
                // YYYY-MM-DD
                String trimmed = inputDate.trim();
                if (trimmed.length() < 10) {
                    return inputDate;
                }
                yyyy = trimmed.substring(0, 4);
                mm = trimmed.substring(5, 7);
                dd = trimmed.substring(8, 10);
            }
            default -> {
                return inputDate;
            }
        }

        return switch (outputType) {
            case "1" -> yyyy + "-" + mm + "-" + dd;
            case "2" -> yyyy + mm + dd;
            default -> inputDate;
        };
    }
}
