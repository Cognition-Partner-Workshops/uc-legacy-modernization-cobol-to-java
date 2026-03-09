package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.service.DateConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DateConverter}, verifying it replicates the COBDATFT
 * assembler routine behaviour used in CBACT01C.
 */
class DateConverterTest {

    @ParameterizedTest(name = "YYYY-MM-DD \"{0}\" -> YYYYMMDD \"{1}\"")
    @CsvSource({
            "2025-05-20, 20250520",
            "2014-11-20, 20141120",
            "2023-01-01, 20230101",
            "2019-12-31, 20191231",
    })
    void convertYyyyMmDdToCompact(String input, String expected) {
        // Input type '2' = YYYY-MM-DD, Output type '2' = YYYYMMDD
        String result = DateConverter.convert(input, DateConverter.INPUT_YYYY_MM_DD, DateConverter.OUTPUT_YYYYMMDD);
        assertEquals(expected, result);
    }

    @ParameterizedTest(name = "YYYYMMDD \"{0}\" -> YYYY-MM-DD \"{1}\"")
    @CsvSource({
            "20250520, 2025-05-20",
            "20141120, 2014-11-20",
    })
    void convertCompactToYyyyMmDd(String input, String expected) {
        // Input type '1' = YYYYMMDD, Output type '1' = YYYY-MM-DD
        String result = DateConverter.convert(input, DateConverter.INPUT_YYYYMMDD, DateConverter.OUTPUT_YYYY_MM_DD);
        assertEquals(expected, result);
    }

    @Test
    void convertYyyymmddToCompact() {
        // Input type 1 (YYYYMMDD), output type 2 (YYYYMMDD) — identity for compact
        String result = DateConverter.convert("20250520", DateConverter.INPUT_YYYYMMDD, DateConverter.OUTPUT_YYYYMMDD);
        assertEquals("20250520", result);
    }

    @Test
    void convertCompactToSeparated() {
        // Input type 1 (YYYYMMDD), output type 1 (YYYY-MM-DD)
        String result = DateConverter.convert("20250520", DateConverter.INPUT_YYYYMMDD, DateConverter.OUTPUT_YYYY_MM_DD);
        assertEquals("2025-05-20", result);
    }

    @Test
    void extractYearFromYyyyMmDd() {
        assertEquals("2025", DateConverter.extractYear("2025-05-20"));
        assertEquals("2014", DateConverter.extractYear("2014-11-20"));
    }

    @Test
    void invalidInputTypeShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", '9', '2'));
    }

    @Test
    void invalidOutputTypeShouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", '2', '9'));
    }
}
