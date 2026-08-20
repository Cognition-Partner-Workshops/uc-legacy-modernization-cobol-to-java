package com.aws.carddemo.common;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;

public final class DateValidator {
  private DateValidator() {}
  public record Result(int severity, int messageCode, String message) {
    public boolean valid() { return severity == 0; }
  }

  public static Result validate(String date, String variableName) {
    String name = variableName == null ? "" : variableName.trim();
    if (date == null || date.isBlank()) return new Result(1, 1, name + " : Date must be supplied.");
    if (date.length() != 8 || !date.chars().allMatch(Character::isDigit)) {
      return new Result(1, 2, name + " must be 8 digit number.");
    }
    int year = Integer.parseInt(date.substring(0, 4));
    int month = Integer.parseInt(date.substring(4, 6));
    int day = Integer.parseInt(date.substring(6, 8));
    if (year < 1900 || year > 2099) return new Result(1, 3, name + " : Century is not valid.");
    if (month < 1 || month > 12) return new Result(1, 4, name + ": Month must be a number between 1 and 12.");
    if (day < 1 || day > 31) return new Result(1, 5, name + ": Day must be a number between 1 and 31.");
    try { LocalDate.of(year, month, day); }
    catch (DateTimeException e) {
      String msg = month == 2 && day == 29 && !Year.isLeap(year)
          ? name + ":Not a leap year.Cannot have 29 days in this month."
          : name + ":Cannot have " + day + " days in this month.";
      return new Result(1, 6, msg);
    }
    return new Result(0, 0, "");
  }

  public static Result validateDateOfBirth(String date, String variableName) {
    Result result = validate(date, variableName);
    if (!result.valid()) return result;
    if (LocalDate.parse(date, java.time.format.DateTimeFormatter.BASIC_ISO_DATE).isAfter(LocalDate.now())) {
      return new Result(1, 7, (variableName == null ? "" : variableName.trim()) + ":cannot be in the future ");
    }
    return result;
  }
}
