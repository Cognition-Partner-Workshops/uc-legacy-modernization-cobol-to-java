package com.carddemo.statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PicEditTest {

    @Test
    void zeroFilledMaskKeepsAllNineIntegerPositions() {
        assertEquals("000000492.00 ", PicEdit.zeroFilled(new BigDecimal("492.00")));
        assertEquals("000000000.00 ", PicEdit.zeroFilled(BigDecimal.ZERO));
        assertEquals("000001234.56 ", PicEdit.zeroFilled(new BigDecimal("1234.56")));
    }

    @Test
    void zeroFilledMaskUsesTrailingMinusForNegatives() {
        assertEquals("000000492.00-", PicEdit.zeroFilled(new BigDecimal("-492.00")));
        assertEquals("000000000.01-", PicEdit.zeroFilled(new BigDecimal("-0.01")));
    }

    @Test
    void zeroFilledMaskTruncatesHighOrderDigitsBeyondNine() {
        assertEquals("234567890.12 ", PicEdit.zeroFilled(new BigDecimal("1234567890.12")));
    }

    @Test
    void zeroSuppressedMaskBlanksLeadingZeros() {
        assertEquals("      453.87 ", PicEdit.zeroSuppressed(new BigDecimal("453.87")));
        assertEquals("     1453.87 ", PicEdit.zeroSuppressed(new BigDecimal("1453.87")));
        assertEquals("      183.88 ", PicEdit.zeroSuppressed(new BigDecimal("183.88")));
        // every Z position is suppressed when the integer part is zero
        assertEquals("         .00 ", PicEdit.zeroSuppressed(BigDecimal.ZERO));
    }

    @Test
    void zeroSuppressedMaskHandlesNegativeAndLargeValues() {
        assertEquals("       47.88-", PicEdit.zeroSuppressed(new BigDecimal("-47.88")));
        assertEquals("123456789.99 ", PicEdit.zeroSuppressed(new BigDecimal("123456789.99")));
        assertEquals("123456789.99-", PicEdit.zeroSuppressed(new BigDecimal("-123456789.99")));
    }

    @Test
    void editedFieldsAreThirteenCharactersWide() {
        assertEquals(13, PicEdit.zeroFilled(new BigDecimal("1.00")).length());
        assertEquals(13, PicEdit.zeroSuppressed(new BigDecimal("1.00")).length());
    }
}
