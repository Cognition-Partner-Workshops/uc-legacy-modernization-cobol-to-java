package com.carddemo.batch.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class DateConverterTest {

    @ParameterizedTest
    @DisplayName("YYYY-MM-DD → YYYYMMDD (type=2 → outType=2, as used by CBACT01C)")
    @CsvSource({
            "2025-05-20, 20250520",
            "2024-08-11, 20240811",
            "2014-01-20, 20140120",
            "2000-12-31, 20001231"
    })
    void isoToCompact(String input, String expected) {
        assertEquals(expected, DateConverter.convert(input, "2", "2"));
    }

    @ParameterizedTest
    @DisplayName("YYYYMMDD → YYYY-MM-DD (type=1 → outType=1)")
    @CsvSource({
            "20250520, 2025-05-20",
            "20240811, 2024-08-11"
    })
    void compactToIso(String input, String expected) {
        assertEquals(expected, DateConverter.convert(input, "1", "1"));
    }

    @ParameterizedTest
    @DisplayName("Cross-format conversions")
    @CsvSource({
            "20250520, 1, 1, 2025-05-20",
            "2025-05-20, 2, 2, 20250520",
            "20250520, 1, 2, 20250520",
            "2025-05-20, 2, 1, 2025-05-20"
    })
    void crossFormat(String input, String inType, String outType, String expected) {
        assertEquals(expected, DateConverter.convert(input, inType, outType));
    }

    @Test
    @DisplayName("Invalid input type throws IllegalArgumentException")
    void invalidInputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "3", "1"));
    }

    @Test
    @DisplayName("Invalid output type throws IllegalArgumentException")
    void invalidOutputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "2", "3"));
    }

    @Test
    @DisplayName("Malformed date throws DateTimeParseException")
    void malformedDate() {
        assertThrows(DateTimeParseException.class,
                () -> DateConverter.convert("not-a-date", "2", "2"));
    }
}
