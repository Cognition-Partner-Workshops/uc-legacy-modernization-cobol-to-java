package com.cardemo.batch.cbact01c;

import com.cardemo.batch.cbact01c.util.DateConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DateConverterTest {

    @Test
    void convertYyyyMmDdToCompact() {
        // The exact conversion CBACT01C performs: type 2 → outtype 2
        assertEquals("20250520", DateConverter.convert("2025-05-20", '2', '2'));
    }

    @Test
    void convertCompactToYyyyMmDd() {
        assertEquals("2025-05-20", DateConverter.convert("20250520", '1', '1'));
    }

    @Test
    void roundTripYyyyMmDd() {
        String original = "2014-11-20";
        String compact = DateConverter.convert(original, '2', '2');
        String restored = DateConverter.convert(compact, '1', '1');
        assertEquals(original, restored);
    }

    @Test
    void invalidInputTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("20250520", '3', '1'));
    }

    @Test
    void tooShortInputThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> DateConverter.convert("2025", '1', '1'));
    }
}
