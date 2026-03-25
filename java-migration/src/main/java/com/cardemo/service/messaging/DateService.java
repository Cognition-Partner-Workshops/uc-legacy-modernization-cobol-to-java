/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Date Service - migrated from COBOL program CODATE01.cbl (app-vsam-mq).
 * Provides date/time utility functions exposed as a service.
 * Original: COBOL utility program for date formatting and conversion,
 *           called by other programs for date operations.
 */
@Service
public class DateService {

    private static final Logger log = LoggerFactory.getLogger(DateService.class);

    /**
     * Get current date and time in various formats.
     * Migrated from GET-CURRENT-DATE paragraph.
     *
     * @return map of date formats and their values
     */
    public Map<String, String> getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> dateMap = new HashMap<>();

        dateMap.put("date-yyyymmdd", now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        dateMap.put("date-mmddyyyy", now.format(DateTimeFormatter.ofPattern("MMddyyyy")));
        dateMap.put("date-formatted", now.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        dateMap.put("time-hhmmss", now.format(DateTimeFormatter.ofPattern("HHmmss")));
        dateMap.put("time-formatted", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        dateMap.put("timestamp", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")));

        return dateMap;
    }

    /**
     * Format a date string.
     * Migrated from FORMAT-DATE paragraph.
     *
     * @param dateStr the date string in YYYYMMDD format
     * @return formatted date string as MM/DD/YYYY
     */
    public String formatDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return dateStr;
        }
        return dateStr.substring(4, 6) + "/" + dateStr.substring(6, 8) + "/" + dateStr.substring(0, 4);
    }

    /**
     * Validate a date string.
     * Migrated from VALIDATE-DATE paragraph.
     *
     * @param dateStr the date string in YYYYMMDD format
     * @return true if valid, false otherwise
     */
    public boolean validateDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return false;
        }
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
