package com.aws.carddemo.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class DateConverterTest {
  @Test
  void convertsBothSupportedLayouts() {
    assertEquals("2024-02-29", DateConverter.toIso("20240229").output());
    assertEquals("20240229", DateConverter.toCobol("2024-02-29").output());
    assertEquals("2024-02-29", DateConverter.fromCobol("20240229").output());
    assertEquals("20240229", DateConverter.fromIso("2024-02-29").output());
  }

  @Test
  void rejectsInvalidAndUnsupportedDates() {
    assertFalse(DateConverter.toIso("20240230").valid());
    assertFalse(DateConverter.toCobol("2024-02-30").valid());
    assertFalse(DateConverter.convert("20240229", "MMDDYYYY", "yyyy-MM-dd").valid());
  }
}
