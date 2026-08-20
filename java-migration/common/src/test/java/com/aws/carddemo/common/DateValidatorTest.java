package com.aws.carddemo.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DateValidatorTest {
  @Test
  void validatesLeapYearsAndMonthLengths() {
    assertTrue(DateValidator.validate("20240229", "DOB").valid());
    assertTrue(DateValidator.validate("20000229", "DOB").valid());
    assertFalse(DateValidator.validate("20230229", "DOB").valid());
    assertFalse(DateValidator.validate("19000229", "DOB").valid());
    assertFalse(DateValidator.validate("20230431", "DOB").valid());
    assertFalse(DateValidator.validate("20230230", "DOB").valid());
  }

  @Test
  void reportsCopybookMessagesForInvalidParts() {
    assertEquals(
        "DOB: Month must be a number between 1 and 12.",
        DateValidator.validate("20231301", "DOB").message());
    assertEquals(
        "DOB:Cannot have 31 days in this month.",
        DateValidator.validate("20230431", "DOB").message());
    assertEquals(
        "DOB : Century is not valid.", DateValidator.validate("21000101", "DOB").message());
  }

  @Test
  void rejectsFutureDateOfBirth() {
    assertFalse(DateValidator.validateDateOfBirth("29990101", "DOB").valid());
  }
}
