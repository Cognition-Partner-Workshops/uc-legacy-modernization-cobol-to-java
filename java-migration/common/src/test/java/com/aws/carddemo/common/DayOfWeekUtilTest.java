package com.aws.carddemo.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;
import org.junit.jupiter.api.Test;

class DayOfWeekUtilTest {
  @Test
  void convertsCobolDateToDayOfWeek() {
    assertEquals(DayOfWeek.THURSDAY, DayOfWeekUtil.dayOfWeek("20240229"));
    assertEquals("THURSDAY", DayOfWeekUtil.cobolDayName("20240229"));
  }
}
