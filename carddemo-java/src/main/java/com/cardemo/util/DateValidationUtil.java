package com.cardemo.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Java equivalent of COBOL program CSUTLDTC.
 * Date validation utility that validates dates against specified formats.
 */
public class DateValidationUtil {

    public static final String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String FORMAT_MM_DD_YYYY = "MM/dd/yyyy";
    public static final String FORMAT_YYYYMMDD = "yyyyMMdd";

    /**
     * Result of a date validation operation.
     */
    public static class ValidationResult {
        private final int severityCode;
        private final String resultMessage;
        private final String testedDate;
        private final String formatUsed;

        public ValidationResult(int severityCode, String resultMessage,
                                String testedDate, String formatUsed) {
            this.severityCode = severityCode;
            this.resultMessage = resultMessage;
            this.testedDate = testedDate;
            this.formatUsed = formatUsed;
        }

        public int getSeverityCode() {
            return severityCode;
        }

        public String getResultMessage() {
            return resultMessage;
        }

        public String getTestedDate() {
            return testedDate;
        }

        public String getFormatUsed() {
            return formatUsed;
        }

        public boolean isValid() {
            return severityCode == 0;
        }
    }

    /**
     * Validates a date string against the specified format.
     * Equivalent of COBOL CSUTLDTC CALL "CEEDAYS" logic.
     *
     * @param dateString the date string to validate
     * @param dateFormat the expected format (e.g., "yyyy-MM-dd")
     * @return ValidationResult with severity code and message
     */
    public ValidationResult validateDate(String dateString, String dateFormat) {
        if (dateString == null || dateString.isBlank()) {
            return new ValidationResult(4, "Insufficient", dateString, dateFormat);
        }

        if (dateFormat == null || dateFormat.isBlank()) {
            return new ValidationResult(4, "Bad Pic String", dateString, dateFormat);
        }

        String javaFormat = convertToJavaFormat(dateFormat);
        if (javaFormat == null) {
            return new ValidationResult(4, "Bad Pic String", dateString, dateFormat);
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(javaFormat)
                    .withResolverStyle(ResolverStyle.STRICT);
            // Use uuuu for strict mode
            String strictFormat = javaFormat.replace("yyyy", "uuuu");
            formatter = DateTimeFormatter.ofPattern(strictFormat)
                    .withResolverStyle(ResolverStyle.STRICT);
            LocalDate parsedDate = LocalDate.parse(dateString, formatter);

            // Additional validations matching COBOL CEEDAYS behavior
            if (parsedDate.getYear() < 1 || parsedDate.getYear() > 9999) {
                return new ValidationResult(4, "Unsupp. Range", dateString, dateFormat);
            }

            int month = parsedDate.getMonthValue();
            if (month < 1 || month > 12) {
                return new ValidationResult(4, "Invalid month", dateString, dateFormat);
            }

            return new ValidationResult(0, "Date is valid", dateString, dateFormat);

        } catch (DateTimeParseException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("could not be parsed")) {
                if (containsNonNumericInDatePart(dateString, javaFormat)) {
                    return new ValidationResult(4, "Nonnumeric data", dateString, dateFormat);
                }
                return new ValidationResult(4, "Datevalue error", dateString, dateFormat);
            }
            return new ValidationResult(4, "Date is invalid", dateString, dateFormat);
        }
    }

    /**
     * Validates a date in YYYY-MM-DD format (most common in CardDemo).
     */
    public ValidationResult validateDateYYYYMMDD(String dateString) {
        return validateDate(dateString, FORMAT_YYYY_MM_DD);
    }

    /**
     * Checks if a date string is before another date string (both YYYY-MM-DD).
     */
    public boolean isBeforeOrEqual(String date1, String date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.compareTo(date2) <= 0;
    }

    /**
     * Checks if a date string represents a date in the future.
     */
    public boolean isFutureDate(String dateString, String format) {
        if (dateString == null || format == null) {
            return false;
        }
        try {
            String javaFormat = convertToJavaFormat(format);
            if (javaFormat == null) {
                return false;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(javaFormat);
            LocalDate date = LocalDate.parse(dateString, formatter);
            return date.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String convertToJavaFormat(String cobolFormat) {
        if (cobolFormat == null) {
            return null;
        }
        switch (cobolFormat) {
            case "YYYY-MM-DD":
            case "yyyy-MM-dd":
                return "yyyy-MM-dd";
            case "MM/DD/YYYY":
            case "MM/dd/yyyy":
                return "MM/dd/yyyy";
            case "YYYYMMDD":
            case "yyyyMMdd":
                return "yyyyMMdd";
            case "DD/MM/YYYY":
            case "dd/MM/yyyy":
                return "dd/MM/yyyy";
            default:
                return null;
        }
    }

    private boolean containsNonNumericInDatePart(String dateString, String format) {
        String digitsOnly = dateString.replaceAll("[^a-zA-Z]", "");
        return !digitsOnly.isEmpty();
    }
}
