package com.carddemo.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Migrated from COBOL program CSUTLDTC.cbl.
 * <p>
 * Date validation utility. The original COBOL program calls the LE runtime
 * CEEDAYS API to validate a date string against a given format mask and
 * returns a message indicating whether the date is valid.
 * <p>
 * In Java, this uses {@link java.time.LocalDate} parsing to validate dates.
 * Accepts a date string and format mask via LINKAGE SECTION equivalent (method params).
 */
public class CsutldtcJob {

    /**
     * Validate a date string against a format mask.
     *
     * @param dateStr    the date string to validate (e.g., "2024-01-15" or "01/15/2024")
     * @param formatMask the COBOL-style format mask (e.g., "YYYY-MM-DD" or "MM/DD/YYYY")
     * @return validation result message (80 chars, matching COBOL LS-RESULT layout)
     */
    public static String validateDate(String dateStr, String formatMask) {
        if (dateStr == null || dateStr.isBlank() || formatMask == null || formatMask.isBlank()) {
            return buildMessage(4, 0, "Insufficient", dateStr, formatMask);
        }

        try {
            DateTimeFormatter formatter = convertCobolMask(formatMask.strip());
            LocalDate.parse(dateStr.strip(), formatter);
            return buildMessage(0, 0, "Date is valid", dateStr, formatMask);
        } catch (DateTimeParseException e) {
            return buildMessage(4, 1, "Datevalue error", dateStr, formatMask);
        } catch (IllegalArgumentException e) {
            return buildMessage(4, 2, "Bad Pic String", dateStr, formatMask);
        }
    }

    /**
     * Convert a COBOL date format mask to a Java DateTimeFormatter.
     * Common COBOL masks:
     * - YYYY-MM-DD → uuuu-MM-dd
     * - YYYYMMDD   → uuuuMMdd
     * - MM/DD/YYYY → MM/dd/uuuu
     */
    private static DateTimeFormatter convertCobolMask(String cobolMask) {
        String javaPattern = cobolMask
                .replace("YYYY", "uuuu")
                .replace("MM", "MM")
                .replace("DD", "dd");
        return DateTimeFormatter.ofPattern(javaPattern);
    }

    /**
     * Build the 80-char WS-MESSAGE result matching the COBOL layout.
     */
    private static String buildMessage(int severity, int msgNo, String result,
                                       String dateStr, String formatMask) {
        StringBuilder sb = new StringBuilder(80);
        sb.append(String.format("%04d", severity));                    // WS-SEVERITY
        sb.append(String.format("Mesg Code:%04d ", msgNo));           // filler + WS-MSG-NO
        sb.append(String.format("%-15s ", result));                   // WS-RESULT
        sb.append(String.format("TstDate:%-10s ",                     // WS-DATE
                dateStr != null ? dateStr : ""));
        sb.append(String.format("Mask used:%-10s ",                   // WS-DATE-FMT
                formatMask != null ? formatMask : ""));
        // Pad/truncate to 80
        String msg = sb.toString();
        if (msg.length() > 80) {
            return msg.substring(0, 80);
        }
        return String.format("%-80s", msg);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: CsutldtcJob <date> <format-mask>");
            System.err.println("  e.g. CsutldtcJob \"2024-01-15\" \"YYYY-MM-DD\"");
            System.exit(1);
        }
        String result = validateDate(args[0], args[1]);
        System.out.println(result);
    }
}
