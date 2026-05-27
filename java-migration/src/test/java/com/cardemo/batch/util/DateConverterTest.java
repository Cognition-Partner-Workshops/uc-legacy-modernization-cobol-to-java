package com.cardemo.batch.util;

import com.cardemo.batch.util.DateConverter.InputFormat;
import com.cardemo.batch.util.DateConverter.OutputFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateConverterTest {

    @ParameterizedTest
    @CsvSource({
            "2025-05-20, 20250520",
            "2014-11-20, 20141120",
            "2024-01-10, 20240110",
            "2000-01-01, 20000101"
    })
    void convertYyyyMmDdToYyyymmdd(String input, String expected) {
        assertEquals(expected,
                DateConverter.convert(input, InputFormat.YYYY_MM_DD, OutputFormat.YYYYMMDD));
    }

    @ParameterizedTest
    @CsvSource({
            "20250520, 2025-05-20",
            "20141120, 2014-11-20"
    })
    void convertYyyymmddToYyyyMmDd(String input, String expected) {
        assertEquals(expected,
                DateConverter.convert(input, InputFormat.YYYYMMDD, OutputFormat.YYYY_MM_DD));
    }

    @Test
    void roundTripConversion() {
        String original = "2025-05-20";
        String compact = DateConverter.convert(original, InputFormat.YYYY_MM_DD, OutputFormat.YYYYMMDD);
        String back = DateConverter.convert(compact, InputFormat.YYYYMMDD, OutputFormat.YYYY_MM_DD);
        assertEquals(original, back);
    }
}
