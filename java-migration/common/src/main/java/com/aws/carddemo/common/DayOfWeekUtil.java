package com.aws.carddemo.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DayOfWeekUtil {
  private DayOfWeekUtil() {}
  public static DayOfWeek dayOfWeek(String yyyymmdd) {
    return LocalDate.parse(yyyymmdd, DateTimeFormatter.BASIC_ISO_DATE).getDayOfWeek();
  }
  public static String cobolDayName(String yyyymmdd) {
    return dayOfWeek(yyyymmdd).name();
  }
}
