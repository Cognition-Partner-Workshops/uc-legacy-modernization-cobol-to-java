package com.carddemo.statement;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class FixedWidth {
    private FixedWidth() {}

    static String field(String line, int start, int end) {
        if (start < 1 || end < start) throw new IllegalArgumentException("Invalid field range");
        String padded = line;
        if (padded.length() < end) padded = String.format("%-" + end + "s", padded);
        return padded.substring(start - 1, end);
    }

    static String trimmed(String line, int start, int end) {
        return field(line, start, end).trim();
    }

    static String accountId(String raw) {
        String value = raw.trim();
        if (!value.matches("\\d+")) {
            throw new IllegalArgumentException("Account id must contain digits: " + raw);
        }
        if (value.length() > 11) {
            throw new IllegalArgumentException("Account id must be at most 11 digits: " + raw);
        }
        return "0".repeat(11 - value.length()) + value;
    }

    static BigDecimal amount(String raw) {
        String value = raw.trim();
        if (value.isEmpty()) return BigDecimal.ZERO.setScale(2);
        boolean negative = false;
        if (value.startsWith("+") || value.startsWith("-")) {
            negative = value.charAt(0) == '-';
            value = value.substring(1);
        } else if (value.endsWith("+") || value.endsWith("-")) {
            negative = value.endsWith("-");
            value = value.substring(0, value.length() - 1);
        }
        if (value.isEmpty()) throw new IllegalArgumentException("Empty numeric value");
        char sign = value.charAt(value.length() - 1);
        if (sign >= 'A' && sign <= 'I') {
            value = value.substring(0, value.length() - 1) + (char) ('1' + sign - 'A');
        } else if (sign >= 'J' && sign <= 'R') {
            negative = true;
            value = value.substring(0, value.length() - 1) + (char) ('1' + sign - 'J');
        } else if (sign == '{' || sign == '}') {
            negative = sign == '}';
            value = value.substring(0, value.length() - 1) + '0';
        }
        BigDecimal result = new BigDecimal(value).movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP);
        return negative && result.signum() != 0 ? result.negate() : result;
    }
}
