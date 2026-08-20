package com.aws.carddemo.common;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;

public final class DateValidator {
  private DateValidator() {}

  public record Result(int severity, int messageCode, String message) {
    public boolean valid() {
      return severity == 0;
    }
  }

  public static Result validate(String date, String variableName) {
    String name = variableName == null ? "" : variableName.trim();
    if (date == null || date.length() < 8) {
      return new Result(1, 1, name + " : Year must be supplied.");
    }
    String yearText = date.substring(0, 4);
    String monthText = date.substring(4, 6);
    String dayText = date.substring(6, 8);
    if (isBlank(yearText)) {
      return new Result(1, 1, name + " : Year must be supplied.");
    }
    if (!isNumeric(yearText)) {
      return new Result(1, 2, name + " must be 4 digit number.");
    }
    int year = Integer.parseInt(yearText);
    if (year < 1900 || year > 2099) {
      return new Result(1, 3, name + " : Century is not valid.");
    }
    if (isBlank(monthText)) {
      return new Result(1, 4, name + " : Month must be supplied.");
    }
    if (!isNumeric(monthText)) {
      return new Result(1, 4, name + ": Month must be a number between 1 and 12.");
    }
    int month = Integer.parseInt(monthText);
    if (month < 1 || month > 12) {
      return new Result(1, 4, name + ": Month must be a number between 1 and 12.");
    }
    if (isBlank(dayText)) {
      return new Result(1, 5, name + " : Day must be supplied.");
    }
    if (!isNumeric(dayText)) {
      return new Result(1, 5, name + ":day must be a number between 1 and 31.");
    }
    int day = Integer.parseInt(dayText);
    if (day < 1 || day > 31) {
      return new Result(1, 5, name + ":day must be a number between 1 and 31.");
    }
    if (day == 31 && !is31DayMonth(month)) {
      return new Result(1, 6, name + ":Cannot have 31 days in this month.");
    }
    if (month == 2 && day == 30) {
      return new Result(1, 6, name + ":Cannot have 30 days in this month.");
    }
    if (month == 2 && day == 29 && !isLeapYear(year)) {
      return new Result(1, 6, name + ":Not a leap year.Cannot have 29 days in this month.");
    }
    try {
      LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    } catch (DateTimeException e) {
      return new Result(1, 6, name + ":Cannot have " + day + " days in this month.");
    }
    return new Result(0, 0, "");
  }

  public static Result validateDateOfBirth(String date, String variableName) {
    Result result = validate(date, variableName);
    if (!result.valid()) {
      return result;
    }
    if (LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE).isAfter(LocalDate.now())) {
      return new Result(
          1, 7, (variableName == null ? "" : variableName.trim()) + ":cannot be in the future ");
    }
    return result;
  }

  public static boolean isLeapYear(int year) {
    return Year.isLeap(year);
  }

  private static boolean isBlank(String value) {
    return value.chars().allMatch(character -> character == 0 || Character.isWhitespace(character));
  }

  private static boolean isNumeric(String value) {
    return value.chars().allMatch(Character::isDigit);
  }

  private static boolean is31DayMonth(int month) {
    return month == 1
        || month == 3
        || month == 5
        || month == 7
        || month == 8
        || month == 10
        || month == 12;
  }
}
