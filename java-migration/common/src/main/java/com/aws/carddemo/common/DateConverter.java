package com.aws.carddemo.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class DateConverter {
  private DateConverter() {}

  public record Result(String output, String errorMessage) {
    public boolean valid() {
      return errorMessage == null;
    }
  }

  public static Result toIso(String yyyymmdd) {
    return convert(yyyymmdd, "yyyyMMdd", "yyyy-MM-dd");
  }

  public static Result toCobol(String iso) {
    return convert(iso, "yyyy-MM-dd", "yyyyMMdd");
  }

  public static Result fromCobol(String yyyymmdd) {
    return toIso(yyyymmdd);
  }

  public static Result fromIso(String iso) {
    return toCobol(iso);
  }

  public static Result convert(String input, String inputPattern, String outputPattern) {
    if (input == null || input.isBlank()) return new Result("", "Input date must be supplied");
    if (!isSupported(inputPattern) || !isSupported(outputPattern)) {
      return new Result("", "Unsupported date format");
    }
    try {
      DateTimeFormatter in = formatter(inputPattern);
      DateTimeFormatter out = formatter(outputPattern);
      LocalDate date = LocalDate.parse(input, in);
      return new Result(date.format(out), null);
    } catch (DateTimeParseException e) {
      return new Result("", "Invalid date: " + input);
    }
  }

  private static boolean isSupported(String pattern) {
    return "yyyyMMdd".equals(pattern) || "yyyy-MM-dd".equals(pattern);
  }

  private static DateTimeFormatter formatter(String pattern) {
    return ("yyyyMMdd".equals(pattern)
            ? DateTimeFormatter.BASIC_ISO_DATE
            : "yyyy-MM-dd".equals(pattern)
                ? DateTimeFormatter.ISO_LOCAL_DATE
                : DateTimeFormatter.ofPattern(pattern))
        .withResolverStyle(ResolverStyle.STRICT);
  }
}
