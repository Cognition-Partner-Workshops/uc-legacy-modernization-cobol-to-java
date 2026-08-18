package com.carddemo.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ZonedDecimalTest {

    @Test
    void decodesPlainDigitsAsPositive() {
        assertEquals(new BigDecimal("504.77"), ZonedDecimal.decode("0000005047" + "7", 2));
        assertEquals(new BigDecimal("0.00"), ZonedDecimal.decode("00000000000", 2));
    }

    @Test
    void decodesPositiveOverpunch() {
        assertEquals(new BigDecimal("504.77"), ZonedDecimal.decode("0000005047G", 2));
        assertEquals(new BigDecimal("0.01"), ZonedDecimal.decode("0000000000A", 2));
        assertEquals(new BigDecimal("0.09"), ZonedDecimal.decode("0000000000I", 2));
        assertEquals(new BigDecimal("183.88"), ZonedDecimal.decode("0000018388", 2));
    }

    @Test
    void decodesNegativeOverpunch() {
        assertEquals(new BigDecimal("-919.00"), ZonedDecimal.decode("0000009190}", 2));
        assertEquals(new BigDecimal("-0.01"), ZonedDecimal.decode("0000000000J", 2));
        assertEquals(new BigDecimal("-0.09"), ZonedDecimal.decode("0000000000R", 2));
        assertEquals(new BigDecimal("-47.88"), ZonedDecimal.decode("0000000478Q", 2));
    }

    @Test
    void decodesZeroOverpunchesWithBothSigns() {
        assertEquals(new BigDecimal("0.00"), ZonedDecimal.decode("0000000000{", 2));
        assertEquals(new BigDecimal("0.00"), ZonedDecimal.decode("0000000000}", 2));
        assertEquals(new BigDecimal("492.00"), ZonedDecimal.decode("00000004920{", 2));
    }

    @Test
    void decodesGnuCobolNativeAsciiNegativeSigns() {
        assertEquals(new BigDecimal("-0.01"), ZonedDecimal.decode("0000000000q", 2));
        assertEquals(new BigDecimal("-919.00"), ZonedDecimal.decode("0000009190p", 2));
    }

    @Test
    void rejectsNonNumericField() {
        assertThrows(IllegalArgumentException.class, () -> ZonedDecimal.decode("00X00000000", 2));
    }
}
