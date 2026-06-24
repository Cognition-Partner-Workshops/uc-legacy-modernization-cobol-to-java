package com.carddemo.batch.converter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class DateConverterTest {

    @Test
    void dashedToCompact_standardDate() {
        assertEquals("20240115", DateConverter.convertDashedToCompact("2024-01-15"));
    }

    @Test
    void dashedToCompact_yearBoundary() {
        assertEquals("20231231", DateConverter.convertDashedToCompact("2023-12-31"));
    }

    @Test
    void compactToDashed_standardDate() {
        assertEquals("2024-01-15", DateConverter.convertCompactToDashed("20240115"));
    }

    @Test
    void compactToDashed_yearBoundary() {
        assertEquals("2023-12-31", DateConverter.convertCompactToDashed("20231231"));
    }

    @ParameterizedTest
    @CsvSource({
        "'2024-01-15', '2', '2', '20240115'",
        "'2024-06-30', '2', '2', '20240630'",
        "'20240115',   '1', '1', '2024-01-15'",
        "'20231231',   '1', '1', '2023-12-31'",
        "'20240115',   '1', '2', '20240115'",
        "'2024-01-15', '2', '1', '2024-01-15'",
    })
    void convert_allTypeCombinations(String input, char inType, char outType, String expected) {
        assertEquals(expected, DateConverter.convert(input, inType, outType));
    }

    @Test
    void convert_paddedInput_type2() {
        assertEquals("20240115", DateConverter.convert("2024-01-15          ", '2', '2'));
    }

    @Test
    void convert_nullInput_returnsNull() {
        assertNull(DateConverter.convert(null, '2', '2'));
    }

    @Test
    void convert_blankInput_returnsBlank() {
        assertEquals("   ", DateConverter.convert("   ", '2', '2'));
    }

    @Test
    void convert_invalidType1Input_tooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2024", '1', '1'));
    }

    @Test
    void convert_invalidType2Input_noDashes() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("20240115xx", '2', '2'));
    }

    @Test
    void convert_unsupportedInputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2024-01-15", '3', '2'));
    }

    @Test
    void convert_unsupportedOutputType() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2024-01-15", '2', '3'));
    }
}
