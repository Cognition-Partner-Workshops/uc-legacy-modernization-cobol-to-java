package com.aws.carddemo.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CobolDecimalTest {
  @Test
  void roundsHalfUpAt005() {
    assertEquals(new BigDecimal("1.24"), CobolDecimal.rounded(new BigDecimal("1.235"), 2));
    assertEquals(new BigDecimal("-1.24"), CobolDecimal.rounded(new BigDecimal("-1.235"), 2));
  }

  @Test
  void roundTripsPositiveAndNegativeZonedOverpunch() {
    assertEquals(new BigDecimal("12.30"), CobolDecimal.decodeZoned("000000123{", 2));
    assertEquals(new BigDecimal("-12.30"), CobolDecimal.decodeZoned("000000123}", 2));
    assertEquals("000000123{", CobolDecimal.encodeZoned(new BigDecimal("12.30"), 8, 2));
    assertEquals("000000123}", CobolDecimal.encodeZoned(new BigDecimal("-12.30"), 8, 2));
  }

  @Test
  void roundTripsPositiveAndNegativeComp3() {
    assertEquals(
        new BigDecimal("12.30"),
        CobolDecimal.decodeComp3(CobolDecimal.encodeComp3(new BigDecimal("12.30"), 4, 2), 2));
    assertEquals(
        new BigDecimal("-12.30"),
        CobolDecimal.decodeComp3(CobolDecimal.encodeComp3(new BigDecimal("-12.30"), 4, 2), 2));
  }
}
