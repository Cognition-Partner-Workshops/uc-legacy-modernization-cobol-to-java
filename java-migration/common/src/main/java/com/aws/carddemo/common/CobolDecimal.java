package com.aws.carddemo.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** Numeric conversions used by the fixed-width CardDemo records. */
public final class CobolDecimal {
  private CobolDecimal() {}

  public static BigDecimal rounded(BigDecimal value, int scale) {
    return Objects.requireNonNull(value, "value").setScale(scale, RoundingMode.HALF_UP);
  }

  public static BigDecimal add(BigDecimal left, BigDecimal right, int scale) {
    return rounded(left.add(right), scale);
  }

  public static BigDecimal subtract(BigDecimal left, BigDecimal right, int scale) {
    return rounded(left.subtract(right), scale);
  }

  public static BigDecimal decodeZoned(String encoded, int scale) {
    Objects.requireNonNull(encoded, "encoded");
    if (encoded.isEmpty()) throw new IllegalArgumentException("zoned decimal is empty");
    char sign = encoded.charAt(encoded.length() - 1);
    boolean negative = sign == '}' || (sign >= 'J' && sign <= 'R');
    char digit =
        switch (sign) {
          case '{', '}' -> '0';
          default -> negative ? (char) ('0' + sign - 'J' + 1) : (char) ('0' + sign - 'A' + 1);
        };
    if (!Character.isDigit(digit) || (!negative && sign != '{' && (sign < 'A' || sign > 'I'))) {
      throw new IllegalArgumentException("invalid zoned decimal sign overpunch: " + sign);
    }
    String digits = encoded.substring(0, encoded.length() - 1) + digit;
    if (!digits.chars().allMatch(Character::isDigit))
      throw new IllegalArgumentException("invalid zoned decimal digits");
    BigDecimal value = new BigDecimal(digits).movePointLeft(scale);
    return negative ? value.negate() : value;
  }

  public static String encodeZoned(BigDecimal value, int integralDigits, int scale) {
    Objects.requireNonNull(value, "value");
    BigDecimal fixed = rounded(value, scale);
    boolean negative = fixed.signum() < 0;
    String digits = fixed.abs().movePointRight(scale).toBigIntegerExact().toString();
    if (digits.length() > integralDigits + scale)
      throw new IllegalArgumentException("value exceeds PIC width");
    digits = "0".repeat(integralDigits + scale - digits.length()) + digits;
    int last = digits.charAt(digits.length() - 1) - '0';
    char overpunch =
        negative
            ? (last == 0 ? '}' : (char) ('J' + last - 1))
            : (last == 0 ? '{' : (char) ('A' + last - 1));
    return digits.substring(0, digits.length() - 1) + overpunch;
  }

  public static BigDecimal decodeComp3(byte[] packed, int scale) {
    Objects.requireNonNull(packed, "packed");
    if (packed.length == 0) throw new IllegalArgumentException("packed decimal is empty");
    StringBuilder digits = new StringBuilder();
    for (int i = 0; i < packed.length - 1; i++) {
      int b = packed[i] & 0xff;
      digits.append((b >>> 4) & 0xf).append(b & 0xf);
    }
    int last = packed[packed.length - 1] & 0xff;
    digits.append((last >>> 4) & 0xf);
    int sign = last & 0xf;
    if (sign != 0xc && sign != 0xd && sign != 0xf)
      throw new IllegalArgumentException("invalid COMP-3 sign");
    BigDecimal result = new BigDecimal(digits.toString()).movePointLeft(scale);
    return sign == 0xd ? result.negate() : result;
  }

  public static byte[] encodeComp3(BigDecimal value, int digits, int scale) {
    BigDecimal fixed = rounded(value, scale);
    String text = fixed.abs().movePointRight(scale).toBigIntegerExact().toString();
    if (text.length() > digits) throw new IllegalArgumentException("value exceeds packed width");
    text = "0".repeat(digits - text.length()) + text;
    String nibbles = ((digits & 1) == 0 ? "0" : "") + text;
    byte[] result = new byte[(nibbles.length() + 1) / 2];
    for (int i = 0; i < nibbles.length() - 1; i += 2) {
      result[i / 2] = (byte) (((nibbles.charAt(i) - '0') << 4) | (nibbles.charAt(i + 1) - '0'));
    }
    result[result.length - 1] =
        (byte)
            (((nibbles.charAt(nibbles.length() - 1) - '0') << 4)
                | (fixed.signum() < 0 ? 0xd : 0xc));
    return result;
  }
}
