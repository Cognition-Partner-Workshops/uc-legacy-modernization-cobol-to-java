package com.carddemo.batch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DateFormatter} — replaces COBDATFT assembler program.
 *
 * Verifies all four input/output type combinations match the assembler logic:
 * <ul>
 *   <li>Type 1→1: YYYYMMDD → YYYY-MM-DD (valid)</li>
 *   <li>Type 1→2: YYYYMMDD → YYYYMMDD (ERROR)</li>
 *   <li>Type 2→2: YYYY-MM-DD → YYYYMMDD (valid)</li>
 *   <li>Type 2→1: YYYY-MM-DD → YYYY-MM-DD (ERROR)</li>
 * </ul>
 */
class DateFormatterTest {

    @Nested
    @DisplayName("Type 1 input (YYYYMMDD)")
    class Type1Input {

        @Test
        @DisplayName("YYYYMMDD → YYYY-MM-DD (type 1→1)")
        void shouldConvertYyyymmddToHyphenated() {
            var result = DateFormatter.convert("1", "20250520", "1");
            assertTrue(result.isSuccess());
            assertEquals("2025-05-20", result.outputDate());
        }

        @Test
        @DisplayName("YYYYMMDD → YYYYMMDD is an error (type 1→2)")
        void shouldRejectType1ToType2() {
            var result = DateFormatter.convert("1", "20250520", "2");
            assertFalse(result.isSuccess());
            assertEquals("INVALID INPUT", result.errorMessage());
        }

        @Test
        @DisplayName("Rejects YYYY-MM-DD as type 1 input (has dash at position 4)")
        void shouldRejectHyphenatedAsType1() {
            var result = DateFormatter.convert("1", "2025-05-20", "1");
            assertFalse(result.isSuccess());
            assertEquals("INVALID INPUT", result.errorMessage());
        }
    }

    @Nested
    @DisplayName("Type 2 input (YYYY-MM-DD)")
    class Type2Input {

        @Test
        @DisplayName("YYYY-MM-DD → YYYYMMDD (type 2→2) — used by CBACT01C")
        void shouldConvertHyphenatedToYyyymmdd() {
            var result = DateFormatter.convert("2", "2025-05-20", "2");
            assertTrue(result.isSuccess());
            assertEquals("20250520", result.outputDate());
        }

        @Test
        @DisplayName("YYYY-MM-DD → YYYY-MM-DD is an error (type 2→1)")
        void shouldRejectType2ToType1() {
            var result = DateFormatter.convert("2", "2025-05-20", "1");
            assertFalse(result.isSuccess());
            assertEquals("INVALID INPUT", result.errorMessage());
        }

        @Test
        @DisplayName("Converts reissue date from sample data")
        void shouldConvertSampleReissueDate() {
            // First record in acctdata.txt: reissue date = 2025-05-20
            var result = DateFormatter.convert("2", "2025-05-20", "2");
            assertTrue(result.isSuccess());
            assertEquals("20250520", result.outputDate());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("Null input returns error")
        void shouldHandleNullInput() {
            var result = DateFormatter.convert("2", null, "2");
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("Blank input returns error")
        void shouldHandleBlankInput() {
            var result = DateFormatter.convert("2", "   ", "2");
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("Invalid type returns error")
        void shouldHandleInvalidType() {
            var result = DateFormatter.convert("3", "20250520", "1");
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("Short input returns error")
        void shouldHandleShortInput() {
            var result = DateFormatter.convert("2", "2025", "2");
            assertFalse(result.isSuccess());
        }
    }
}
