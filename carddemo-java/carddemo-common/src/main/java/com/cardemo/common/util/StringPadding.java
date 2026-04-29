package com.cardemo.common.util;

/**
 * String padding utility replacing COBOL copybook CSSTRPFY.cpy functionality.
 * Source: app/cpy/CSSTRPFY.cpy
 *
 * Provides left/right padding operations commonly used in COBOL for fixed-width fields.
 *
 * TODO: Add EBCDIC-to-UTF8 conversion support if needed during data migration
 */
public final class StringPadding {

    private StringPadding() {
    }

    public static String padRight(String value, int length) {
        if (value == null) {
            return " ".repeat(length);
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return value + " ".repeat(length - value.length());
    }

    public static String padLeft(String value, int length, char padChar) {
        if (value == null) {
            return String.valueOf(padChar).repeat(length);
        }
        if (value.length() >= length) {
            return value.substring(0, length);
        }
        return String.valueOf(padChar).repeat(length - value.length()) + value;
    }

    public static String zeroPad(long number, int length) {
        return padLeft(String.valueOf(number), length, '0');
    }

    public static String trimAndTruncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }
}
