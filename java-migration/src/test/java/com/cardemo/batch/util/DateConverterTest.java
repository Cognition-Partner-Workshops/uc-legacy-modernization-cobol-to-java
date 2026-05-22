package com.cardemo.batch.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateConverterTest {

    @ParameterizedTest
    @CsvSource({
            "2023-06-01, 2, 2, 20230601",
            "2023-06-01, 2, 1, 2023-06-01",
            "20230601,   1, 1, 2023-06-01",
            "20230601,   1, 2, 20230601",
            "2020-01-15, 2, 2, 20200115",
            "20200115,   1, 1, 2020-01-15"
    })
    void convert_variousFormats(String input, String inputType, String outputType, String expected) {
        assertEquals(expected, DateConverter.convert(input.trim(), inputType.trim(), outputType.trim()));
    }

    @Test
    void convert_blankInput_returnsEmpty() {
        assertEquals("", DateConverter.convert("", "2", "2"));
        assertEquals("", DateConverter.convert("   ", "2", "2"));
    }

    @Test
    void convert_nullInput_returnsEmpty() {
        assertEquals("", DateConverter.convert(null, "2", "2"));
    }

    @Test
    void convert_unknownInputType_returnsInputUnchanged() {
        assertEquals("2023-06-01", DateConverter.convert("2023-06-01", "9", "2"));
    }

    @Test
    void convert_unknownOutputType_returnsInputUnchanged() {
        assertEquals("2023-06-01", DateConverter.convert("2023-06-01", "2", "9"));
    }
}
