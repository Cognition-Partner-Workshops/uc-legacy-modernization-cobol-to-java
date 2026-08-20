package com.aws.carddemo.common;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CommonUtilityTest {
  @Test void decimalRoundingAndOverpunchRoundTrip() {
    assertEquals(new BigDecimal("1.24"), CobolDecimal.rounded(new BigDecimal("1.235"), 2));
    assertEquals(new BigDecimal("-12.30"), CobolDecimal.decodeZoned("000000123}", 2));
    assertEquals("000000123}", CobolDecimal.encodeZoned(new BigDecimal("-12.30"), 8, 2));
    assertEquals(new BigDecimal("12.30"), CobolDecimal.decodeComp3(CobolDecimal.encodeComp3(new BigDecimal("12.30"), 4, 2), 2));
  }
  @Test void dateAndLookupRules() {
    assertTrue(DateValidator.validate("20240229", "DOB").valid());
    assertFalse(DateValidator.validate("20230229", "DOB").valid());
    assertFalse(DateValidator.validate("20231301", "DOB").valid());
    assertEquals("2024-02-29", DateConverter.toIso("20240229").output());
    assertFalse(DateConverter.toIso("20240230").valid());
    assertEquals("THURSDAY", DayOfWeekUtil.cobolDayName("20240229"));
    assertTrue(LookupTables.isValidStateZipPrefix("NC", "27514"));
    assertTrue(LookupTables.isValidPhoneAreaCode("908"));
    assertFalse(LookupTables.isValidState("ZZ"));
  }
}
