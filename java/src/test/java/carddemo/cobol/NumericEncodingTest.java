package carddemo.cobol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class NumericEncodingTest {

    @Test
    void zonedPositiveUsesTrailingOverpunch() {
        byte[] out = ZonedDecimal.encode(new BigDecimal("1945.87"), 12, 2, true);
        assertEquals("00000019458G", new String(out, StandardCharsets.US_ASCII));
        assertEquals(new BigDecimal("1945.87"), ZonedDecimal.decode(out, 0, 12, 2, true));
    }

    @Test
    void zonedNegativeUsesJtoRAndBraceForZeroDigit() {
        assertEquals("00000000478Q", new String(ZonedDecimal.encode(new BigDecimal("-47.88"), 12, 2, true),
                StandardCharsets.US_ASCII));
        assertEquals("00000010250}", new String(ZonedDecimal.encode(new BigDecimal("-1025.00"), 12, 2, true),
                StandardCharsets.US_ASCII));
        assertEquals(new BigDecimal("-1025.00"),
                ZonedDecimal.decode("00000010250}".getBytes(StandardCharsets.US_ASCII), 0, 12, 2, true));
    }

    @Test
    void unsignedZonedIsPlainDigitsAndRejectsNonDigits() {
        byte[] out = ZonedDecimal.encode(new BigDecimal("50"), 11, 0, false);
        assertEquals("00000000050", new String(out, StandardCharsets.US_ASCII));
        assertEquals(new BigDecimal("50"), ZonedDecimal.decode(out, 0, 11, 0, false));
        byte[] bad = "0000000005A".getBytes(StandardCharsets.US_ASCII);
        assertThrows(IllegalArgumentException.class, () -> ZonedDecimal.decode(bad, 0, 11, 0, false));
    }

    @Test
    void zonedStoreTruncatesDecimalsAndHighOrderDigits() {
        assertEquals("1234", new String(ZonedDecimal.encode(new BigDecimal("91234.999"), 4, 0, false),
                StandardCharsets.US_ASCII));
        assertEquals("12345", new String(ZonedDecimal.encode(new BigDecimal("123.456"), 5, 2, false),
                StandardCharsets.US_ASCII));
    }

    @Test
    void zonedMoveZeroIsSignedButInitializeIsPlainDigits() {
        RecordArea area = new RecordArea(12);
        RecordArea.Num field = new RecordArea.Num(0, 12, 2, true);
        area.set(field, 0);
        assertEquals("00000000000{", new String(area.bytes(), StandardCharsets.US_ASCII));
        area.initialize(field);
        assertEquals("000000000000", new String(area.bytes(), StandardCharsets.US_ASCII));
        assertEquals(BigDecimal.ZERO.setScale(2), area.get(field));
    }

    @Test
    void packedDecimalRoundTripsWithSignNibble() {
        byte[] positive = PackedDecimal.encode(new BigDecimal("2525.00"), 12, 2, true);
        assertEquals(7, positive.length);
        assertArrayEquals(new byte[] {0, 0, 0, 0x02, 0x52, 0x50, 0x0C}, positive);
        assertEquals(new BigDecimal("2525.00"), PackedDecimal.decode(positive, 0, 12, 2));

        byte[] negative = PackedDecimal.encode(new BigDecimal("-2500.00"), 12, 2, true);
        assertEquals(0x0D, negative[6]);
        assertEquals(new BigDecimal("-2500.00"), PackedDecimal.decode(negative, 0, 12, 2));

        byte[] unsigned = PackedDecimal.encode(new BigDecimal("123"), 5, 0, false);
        assertArrayEquals(new byte[] {0x00, 0x12, 0x3F}, unsigned);
        assertEquals(new BigDecimal("123"), PackedDecimal.decode(unsigned, 0, 5, 0));
    }

    @Test
    void displayTextMatchesCobolDisplayOfNumericField() {
        assertEquals("000000019400+", ZonedDecimal.displayText(new BigDecimal("194.00"), 12, 2, true));
        assertEquals("000000000047-", ZonedDecimal.displayText(new BigDecimal("-0.479"), 12, 2, true));
        assertEquals("000000300", ZonedDecimal.displayText(new BigDecimal("300"), 9, 0, false));
    }

    @Test
    void alnumMoveLeftJustifiesAndSpaceFills() {
        RecordArea area = new RecordArea(6);
        RecordArea.Alnum field = new RecordArea.Alnum(1, 4);
        area.set(field, "AB");
        assertEquals(" AB   ", new String(area.bytes(), StandardCharsets.US_ASCII));
        area.set(field, "ABCDEFG");
        assertEquals(" ABCD ", new String(area.bytes(), StandardCharsets.US_ASCII));
    }
}
