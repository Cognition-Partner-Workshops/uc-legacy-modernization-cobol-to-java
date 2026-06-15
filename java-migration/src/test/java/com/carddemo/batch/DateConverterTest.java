package com.carddemo.batch;

import com.carddemo.batch.util.DateConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateConverterTest {

    @ParameterizedTest
    @CsvSource({
            "'2025-05-20', '20250520  '",
            "'2014-11-20', '20141120  '",
            "'2024-01-01', '20240101  '",
    })
    void convertDashToCompact(String input, String expected) {
        assertEquals(expected, DateConverter.convertDashToCompact(input));
    }

    @Test
    void convertDashToCompactPaddedToTenChars() {
        String result = DateConverter.convertDashToCompact("2025-05-20");
        assertEquals(10, result.length());
    }

    @Test
    void extractYear() {
        assertEquals("2025", DateConverter.extractYear("2025-05-20"));
        assertEquals("2014", DateConverter.extractYear("2014-11-20"));
    }

    @Test
    void extractYearFromBlank() {
        assertEquals("    ", DateConverter.extractYear(null));
        assertEquals("    ", DateConverter.extractYear(""));
    }
}
