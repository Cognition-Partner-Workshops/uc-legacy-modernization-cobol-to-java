package com.cardemo.batch.service;

/**
 * Replaces the COBDATFT assembler subroutine called via CODATECN copybook.
 * Converts dates between formats:
 *   Type 1: YYYYMMDD
 *   Type 2: YYYY-MM-DD
 *
 * The COBOL program calls this with:
 *   CODATECN-TYPE = '2' (input is YYYY-MM-DD)
 *   CODATECN-OUTTYPE = '2' (output is YYYYMMDD)
 */
public final class DateFormatService {

    private DateFormatService() {}

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
                yyyy = inputDate.substring(0, 4);
                mm = inputDate.substring(4, 6);
                dd = inputDate.substring(6, 8);
            }
            case "2" -> {
                // YYYY-MM-DD
                String[] parts = inputDate.split("-");
                if (parts.length < 3) {
                    return inputDate;
                }
                yyyy = parts[0];
                mm = parts[1];
                dd = parts[2];
            }
            default -> {
                return inputDate;
            }
        }

        return switch (outputType) {
            case "1" -> yyyy + "-" + mm + "-" + dd;   // output YYYY-MM-DD
            case "2" -> yyyy + mm + dd;               // output YYYYMMDD
            default -> inputDate;
        };
    }

    /**
     * Extracts the 4-digit year from a date string in YYYY-MM-DD format.
     */
    public static String extractYear(String dateStr) {
        if (dateStr == null || dateStr.length() < 4) {
            return "    ";
        }
        return dateStr.substring(0, 4);
    }
}
