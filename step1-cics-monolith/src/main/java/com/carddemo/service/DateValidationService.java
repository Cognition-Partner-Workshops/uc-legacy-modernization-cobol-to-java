package com.carddemo.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Mirrors CSUTLDTC.cbl - Date validation utility.
 */
@Service
public class DateValidationService {

    public record ValidationResult(String severityCode, String messageNumber, String message) {

        public boolean isValid() {
            return "0000".equals(severityCode);
        }
    }

    /**
     * Validate a date string against a format.
     * Mirrors CSUTLDTC.cbl validation logic.
     */
    public ValidationResult validateDate(String dateStr, String format) {
        if (dateStr == null || dateStr.isBlank()) {
            return new ValidationResult("0008", "0001", "Date is empty");
        }

        if (format == null || format.isBlank()) {
            return new ValidationResult("0008", "0002", "Date format is empty");
        }

        try {
            DateTimeFormatter formatter;
            if ("YYYY-MM-DD".equalsIgnoreCase(format)) {
                formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);
            } else if ("MM/DD/YY".equalsIgnoreCase(format)) {
                formatter = DateTimeFormatter.ofPattern("MM/dd/uu")
                    .withResolverStyle(ResolverStyle.STRICT);
            } else {
                formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")
                    .withResolverStyle(ResolverStyle.STRICT);
            }

            LocalDate date = LocalDate.parse(dateStr.trim(), formatter);

            // Validate ranges like COBOL does
            if (date.getMonthValue() < 1 || date.getMonthValue() > 12) {
                return new ValidationResult("0008", "0003", "Month is not valid");
            }
            if (date.getDayOfMonth() < 1 || date.getDayOfMonth() > 31) {
                return new ValidationResult("0008", "0004", "Day is not valid");
            }

            return new ValidationResult("0000", "0000", "Date is valid");
        } catch (DateTimeParseException e) {
            return new ValidationResult("0008", "0005", "Date format is invalid: " + e.getMessage());
        }
    }

    /**
     * Validate date in YYYY-MM-DD format specifically.
     */
    public ValidationResult validateYyyyMmDd(String dateStr) {
        return validateDate(dateStr, "YYYY-MM-DD");
    }
}
