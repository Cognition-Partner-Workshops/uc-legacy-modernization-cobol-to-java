package com.carddemo.statement.parser;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class FixedWidth {
  private FixedWidth() {}

  static String field(String record, int start, int length) {
    if (start >= record.length()) return "";
    return record.substring(start, Math.min(record.length(), start + length)).stripTrailing();
  }

  static BigDecimal zonedDecimal(String record, int start, int length) {
    String value = record.substring(start, Math.min(record.length(), start + length));
    if (value.isEmpty()) throw new IllegalArgumentException("Missing numeric field");
    char last = value.charAt(value.length() - 1);
    boolean negative = last == '}' || (last >= 'J' && last <= 'R');
    int digit = switch (last) {
      case '{', '}' -> 0;
      default -> negative ? last - 'J' + 1 : last - 'A' + 1;
    };
    String digits = value.substring(0, value.length() - 1) + digit;
    if (!digits.chars().allMatch(Character::isDigit)) {
      throw new IllegalArgumentException("Malformed zoned decimal: " + value);
    }
    BigDecimal amount = new BigDecimal(digits).movePointLeft(2).setScale(2, RoundingMode.UNNECESSARY);
    return negative ? amount.negate() : amount;
  }
}
