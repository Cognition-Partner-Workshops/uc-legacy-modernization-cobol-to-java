package com.carddemo.dataloader.parser;

import java.math.BigDecimal;

public final class FixedWidthParser {

    private FixedWidthParser() {}

    public static String extractString(String line, int start, int length) {
        if (line == null || start >= line.length()) {
            return "";
        }
        int end = Math.min(start + length, line.length());
        return line.substring(start, end).trim();
    }

    public static BigDecimal extractSignedDecimal(String line, int start, int length, int decimalPlaces) {
        String raw = extractString(line, start, length);
        if (raw.isEmpty()) {
            return BigDecimal.ZERO;
        }

        boolean negative = false;
        StringBuilder cleaned = new StringBuilder();

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '-') {
                negative = true;
            } else if (c == '+') {
                // positive, ignore
            } else if (c == '{') {
                cleaned.append('0');
            } else if (c == '}') {
                cleaned.append('0');
                negative = true;
            } else if (c >= 'A' && c <= 'I') {
                cleaned.append((char) ('1' + (c - 'A')));
            } else if (c >= 'J' && c <= 'R') {
                cleaned.append((char) ('1' + (c - 'J')));
                negative = true;
            } else if (Character.isDigit(c)) {
                cleaned.append(c);
            }
        }

        if (cleaned.length() == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = new BigDecimal(cleaned.toString());
        if (decimalPlaces > 0) {
            result = result.movePointLeft(decimalPlaces);
        }
        if (negative) {
            result = result.negate();
        }
        return result;
    }

    public static int extractInt(String line, int start, int length) {
        String raw = extractString(line, start, length);
        if (raw.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
