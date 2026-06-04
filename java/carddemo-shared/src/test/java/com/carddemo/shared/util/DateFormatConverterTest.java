package com.carddemo.shared.util;

import com.carddemo.shared.model.ConversionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link DateFormatConverter} (port of Assembler {@code COBDATFT}). */
class DateFormatConverterTest {

    private final DateFormatConverter converter = new DateFormatConverter();

    @Test
    void isoToUsConvertsValidDate() {
        assertEquals("01/15/2023", converter.isoToUs("2023-01-15"));
        assertEquals("12/31/1999", converter.isoToUs("1999-12-31"));
    }

    @Test
    void usToIsoConvertsValidDate() {
        assertEquals("2023-01-15", converter.usToIso("01/15/2023"));
        assertEquals("1999-12-31", converter.usToIso("12/31/1999"));
    }

    @Test
    void roundTripsAreStable() {
        String iso = "2024-02-29";
        assertEquals(iso, converter.usToIso(converter.isoToUs(iso)));
    }

    @Test
    void handlesLeapDay() {
        assertEquals("02/29/2024", converter.isoToUs("2024-02-29"));
        assertEquals("2024-02-29", converter.usToIso("02/29/2024"));
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertEquals("01/15/2023", converter.isoToUs("  2023-01-15 "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2023-02-30", "2023-13-01", "01/15/2023", "2023/01/15", "not-a-date"})
    void isoToUsRejectsInvalidInput(String bad) {
        assertThrows(IllegalArgumentException.class, () -> converter.isoToUs(bad));
    }

    @ParameterizedTest
    @ValueSource(strings = {"13/01/2023", "02/30/2023", "2023-01-15", "1/5/2023", "not-a-date"})
    void usToIsoRejectsInvalidInput(String bad) {
        assertThrows(IllegalArgumentException.class, () -> converter.usToIso(bad));
    }

    @Test
    void isoToUsRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> converter.isoToUs(null));
    }

    @Test
    void convertType1DelegatesToIsoToUs() {
        ConversionResult result = converter.convert("1", "2023-01-15");
        assertTrue(result.success());
        assertEquals("01/15/2023", result.outputDate());
        assertNull(result.errorMessage());
    }

    @Test
    void convertType2DelegatesToUsToIso() {
        ConversionResult result = converter.convert("2", "01/15/2023");
        assertTrue(result.success());
        assertEquals("2023-01-15", result.outputDate());
        assertNull(result.errorMessage());
    }

    @Test
    void convertReturnsErrorForUnknownType() {
        ConversionResult result = converter.convert("3", "2023-01-15");
        assertFalse(result.success());
        assertNull(result.outputDate());
        assertEquals(ConversionResult.INVALID_INPUT, result.errorMessage());
    }

    @Test
    void convertReturnsErrorForNullTypeOrBadDate() {
        assertFalse(converter.convert(null, "2023-01-15").success());
        ConversionResult result = converter.convert("1", "bad-date");
        assertFalse(result.success());
        assertEquals(ConversionResult.INVALID_INPUT, result.errorMessage());
    }
}
