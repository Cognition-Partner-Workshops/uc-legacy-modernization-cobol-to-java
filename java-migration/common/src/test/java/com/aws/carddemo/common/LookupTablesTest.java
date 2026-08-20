package com.aws.carddemo.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LookupTablesTest {
  @Test
  void validatesCopybookStateCodesAndZipPrefixes() {
    assertTrue(LookupTables.isValidState("NC"));
    assertTrue(LookupTables.isValidStateZipPrefix("NC", "27514"));
    assertTrue(LookupTables.isValidStateZipPrefix("NJ", "89001"));
    assertFalse(LookupTables.isValidState("FM"));
    assertFalse(LookupTables.isValidStateZipPrefix("NC", "99999"));
    assertFalse(LookupTables.isValidStateZipPrefix("ZZ", "27514"));
  }

  @Test
  void validatesCopybookPhoneAreaCodes() {
    assertTrue(LookupTables.isValidPhoneAreaCode("201"));
    assertTrue(LookupTables.isValidPhoneAreaCode("908"));
    assertFalse(LookupTables.isValidPhoneAreaCode("221"));
    assertFalse(LookupTables.isValidPhoneAreaCode("ZZZ"));
  }
}
