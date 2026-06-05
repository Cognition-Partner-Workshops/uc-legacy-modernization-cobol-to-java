package com.carddemo.batch.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DateConverterTest {

    @ParameterizedTest(name = "convert \"{0}\" (type {1} → {2}) == \"{3}\"")
    @CsvSource({
            "'2025-05-20', 2, 2, 20250520",
            "'2024-08-11', 2, 2, 20240811",
            "'2013-06-19', 2, 2, 20130619",
            "'2025-05-20', 2, 1, 2025-05-20",
            "'20250520',   1, 1, 2025-05-20",
            "'20250520',   1, 2, 20250520",
    })
    void convertShouldReformatCorrectly(String input, String inType,
                                        String outType, String expected) {
        assertEquals(expected, DateConverter.convert(input, inType, outType));
    }

    @Test
    void convertYyyyMmDdToCompactMatchesCobolCobdatft() {
        // This is the exact transformation CBACT01C performs:
        // input type '2' (YYYY-MM-DD) → output type '2' (YYYYMMDD)
        String result = DateConverter.convert("2025-05-20", "2", "2");
        assertEquals("20250520", result);
    }

    @Test
    void convertCompactToHyphenated() {
        String result = DateConverter.convert("20240811", "1", "1");
        assertEquals("2024-08-11", result);
    }

    @Test
    void rejectsInvalidInputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "3", "2"));
    }

    @Test
    void rejectsInvalidOutputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025-05-20", "2", "3"));
    }
}
