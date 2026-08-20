package com.aws.carddemo.batch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.batch.core.JobParameters;

final class BatchSupport {
  static final DateTimeFormatter DB2_TIMESTAMP =
      DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

  private BatchSupport() {}

  static String timestamp() {
    return LocalDateTime.now().format(DB2_TIMESTAMP);
  }

  static BigDecimal value(BigDecimal value) {
    return value == null ? BigDecimal.ZERO.setScale(2) : value;
  }

  static String parameter(JobParameters parameters, String name, String fallback) {
    return Optional.ofNullable(parameters.getString(name))
        .filter(s -> !s.isBlank())
        .orElse(fallback);
  }

  static String dateOnly(String timestamp) {
    return timestamp == null || timestamp.length() < 10 ? "" : timestamp.substring(0, 10);
  }

  static String pad(String value, int width) {
    if (value == null) {
      value = "";
    }
    return String.format("%-" + width + "s", value).substring(0, width);
  }
}
