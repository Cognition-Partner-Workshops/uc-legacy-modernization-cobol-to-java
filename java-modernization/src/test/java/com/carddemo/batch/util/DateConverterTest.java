package com.carddemo.batch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DateConverterTest {

    @ParameterizedTest(name = "YYYY-MM-DD \"{0}\" → YYYYMMDD \"{1}\"")
    @CsvSource({
            "2025-05-20, 20250520",
            "2024-08-11, 20240811",
            "2023-01-01, 20230101",
            "2019-12-31, 20191231",
    })
    @DisplayName("convert: YYYY-MM-DD (type 2 in) → YYYYMMDD (type 2 out)")
    void convertYyyyMmDdToYyyymmdd(String input, String expected) {
        String result = DateConverter.convert(input, "2", "2");
        assertEquals(expected, result);
    }

    @ParameterizedTest(name = "YYYYMMDD \"{0}\" → YYYY-MM-DD \"{1}\"")
    @CsvSource({
            "20250520, 2025-05-20",
            "20240811, 2024-08-11",
    })
    @DisplayName("convert: YYYYMMDD (type 1 in) → YYYY-MM-DD (type 1 out)")
    void convertYyyymmddToYyyyMmDd(String input, String expected) {
        String result = DateConverter.convert(input, "1", "1");
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("convert: unsupported input type throws")
    void unsupportedInputTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "9", "2"));
    }

    @Test
    @DisplayName("convert: unsupported output type throws")
    void unsupportedOutputTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "2", "9"));
    }

    @Test
    @DisplayName("convert: short input throws")
    void shortInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025", "2", "2"));
    }

    @ParameterizedTest(name = "extractYear(\"{0}\") → \"{1}\"")
    @CsvSource({
            "2025-05-20, 2025",
            "2024-08-11, 2024",
            "2013-06-19, 2013",
    })
    @DisplayName("extractYear returns the 4-digit year from YYYY-MM-DD")
    void extractYear(String input, String expected) {
        assertEquals(expected, DateConverter.extractYear(input));
    }

    @Test
    @DisplayName("extractYear: null returns spaces")
    void extractYearNullReturnsSpaces() {
        assertEquals("    ", DateConverter.extractYear(null));
    }
}
