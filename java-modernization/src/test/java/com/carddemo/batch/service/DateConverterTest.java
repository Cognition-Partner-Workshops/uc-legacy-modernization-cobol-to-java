package com.carddemo.batch.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DateConverter}, the Java replacement for the
 * COBOL assembler subroutine COBDATFT.
 *
 * <p>CODATECN copybook type mapping:
 * <ul>
 *   <li>Input type 1 = YYYYMMDD, Input type 2 = YYYY-MM-DD</li>
 *   <li>Output type 1 = YYYY-MM-DD, Output type 2 = YYYYMMDD</li>
 * </ul>
 */
class DateConverterTest {

    @Test
    @DisplayName("YYYY-MM-DD → YYYYMMDD (type 2 → type 2 output, as used in CBACT01C)")
    void hyphenatedToCompact() {
        // This is the exact conversion the COBOL program performs:
        //   MOVE '2' TO CODATECN-TYPE.
        //   MOVE '2' TO CODATECN-OUTTYPE.
        String result = DateConverter.convert("2025-05-20", "2", "2");
        assertEquals("20250520", result);
    }

    @Test
    @DisplayName("YYYYMMDD → YYYY-MM-DD (type 1 → type 1 output)")
    void compactToHyphenated() {
        String result = DateConverter.convert("20250520", "1", "1");
        assertEquals("2025-05-20", result);
    }

    @Test
    @DisplayName("YYYYMMDD → YYYYMMDD (type 1 → type 2)")
    void compactToCompact() {
        String result = DateConverter.convert("20140312", "1", "2");
        assertEquals("20140312", result);
    }

    @Test
    @DisplayName("YYYY-MM-DD → YYYY-MM-DD (type 2 → type 1)")
    void hyphenatedToHyphenated() {
        String result = DateConverter.convert("2014-03-12", "2", "1");
        assertEquals("2014-03-12", result);
    }

    @ParameterizedTest(name = "convert(\"{0}\", 2, 2) = \"{1}\"")
    @CsvSource({
            "2014-11-20, 20141120",
            "2025-05-20, 20250520",
            "2013-06-19, 20130619",
            "2024-08-11, 20240811",
            "2023-01-27, 20230127",
            "2009-06-17, 20090617"
    })
    @DisplayName("Batch conversion of sample reissue dates (input type 2 \u2192 output type 2)")
    void batchHyphenatedToCompact(String input, String expected) {
        assertEquals(expected, DateConverter.convert(input, "2", "2"));
    }

    @Test
    @DisplayName("extractYear returns first 4 chars of YYYY-MM-DD")
    void extractYear() {
        assertEquals("2025", DateConverter.extractYear("2025-05-20"));
        assertEquals("2013", DateConverter.extractYear("2013-06-19"));
    }

    @Test
    @DisplayName("extractYear handles null/short input gracefully")
    void extractYearEdge() {
        assertEquals("    ", DateConverter.extractYear(null));
        assertEquals("    ", DateConverter.extractYear(""));
        assertEquals("    ", DateConverter.extractYear("  "));
    }

    @Test
    @DisplayName("Unknown input type throws")
    void unknownInputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "9", "2"));
    }

    @Test
    @DisplayName("Unknown output type throws")
    void unknownOutputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "2", "9"));
    }

    @Test
    @DisplayName("Null/short date throws for compact type")
    void nullCompactThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert(null, "1", "2"));
    }

    @Test
    @DisplayName("Null/short date throws for hyphenated type")
    void nullHyphenatedThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert(null, "2", "2"));
    }
}
