package carddemo.cobol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CodecTest {

    @ParameterizedTest
    @CsvSource({
        "1940.00,     00000019400{",
        "-1025.37,    00000010253P",
        "0,           00000000000{",
        "20.01,       00000000200A",
        "-0.09,       00000000000R",
        "9999999999.99, 99999999999I"
    })
    void zonedSignedRoundTrip(String value, String image) {
        byte[] encoded = ZonedDecimal.encode(new BigDecimal(value), 12, 2, true);
        assertEquals(image, Bytes.ascii(encoded, 0, 12));
        assertEquals(0, new BigDecimal(value).compareTo(ZonedDecimal.decode(encoded, 0, 12, 2, true)));
    }

    @Test
    void zonedUnsignedHasNoOverpunch() {
        assertEquals("00000000050", Bytes.ascii(ZonedDecimal.encode(BigDecimal.valueOf(50), 11, 0, false), 0, 11));
    }

    @Test
    void zonedTruncatesLikeCobolMove() {
        // Extra decimals dropped, high-order digits lost (no ROUNDED / ON SIZE ERROR).
        assertEquals("00000000012C",
                Bytes.ascii(ZonedDecimal.encode(new BigDecimal("1.239"), 12, 2, true), 0, 12));
        assertEquals("000000000001",
                Bytes.ascii(ZonedDecimal.encode(new BigDecimal("1000000000001"), 12, 0, false), 0, 12));
    }

    @Test
    void zonedPlainTrailingDigitIsPositive() {
        assertEquals(new BigDecimal("12.34"), ZonedDecimal.decode(Bytes.ascii("000000001234"), 0, 12, 2, true));
    }

    @Test
    void zonedNegativeZeroKeepsItsSign() {
        byte[] image = Bytes.ascii("00000000000}");
        assertEquals(0, ZonedDecimal.decode(image, 0, 12, 2, true).signum());
        assertTrue(ZonedDecimal.isNegative(image, 0, 12));
        assertEquals("000000000000-", ZonedDecimal.displayText(BigDecimal.ZERO, 12, 2, true, true));
        assertEquals("00000000000}", Bytes.ascii(ZonedDecimal.encode(BigDecimal.ZERO, 12, 2, true, true), 0, 12));
    }

    @Test
    void zonedRejectsNonNumericBytes() {
        assertThrows(NumberFormatException.class, () -> ZonedDecimal.decode(Bytes.ascii("0000 0000000"), 0, 12, 2, true));
    }

    @Test
    void displayTextMatchesGnuCobol() {
        assertEquals("000000194000+", ZonedDecimal.displayText(new BigDecimal("1940.00"), 12, 2, true, false));
        assertEquals("000000102537-", ZonedDecimal.displayText(new BigDecimal("-1025.37"), 12, 2, true, true));
        assertEquals("00000000050", ZonedDecimal.displayText(BigDecimal.valueOf(50), 11, 0, false, false));
    }

    @ParameterizedTest
    @CsvSource({
        "2525.00,  0000000252500C",
        "-2500.00, 0000000250000D",
        "1005.00,  0000000100500C",
        "0,        0000000000000C"
    })
    void packedS9_10V99(String value, String hex) {
        byte[] encoded = PackedDecimal.encode(new BigDecimal(value), 12, 2, true);
        assertEquals(7, encoded.length);
        assertEquals(hex, hex(encoded));
        assertEquals(0, new BigDecimal(value).compareTo(PackedDecimal.decode(encoded, 0, 12, 2)));
    }

    @Test
    void packedOddDigitsAndUnsigned() {
        assertEquals("12345F", hex(PackedDecimal.encode(BigDecimal.valueOf(12345), 5, 0, false)));
        assertEquals("12345D", hex(PackedDecimal.encode(BigDecimal.valueOf(-12345), 5, 0, true)));
        assertEquals(3, PackedDecimal.byteLength(5));
    }

    @Test
    void packedRejectsInvalidNibble() {
        assertThrows(NumberFormatException.class, () -> PackedDecimal.decode(new byte[] {(byte) 0xA0, 0x0C}, 0, 3, 0));
    }

    @Test
    void recordAreaMoveAndInitialize() {
        RecordArea area = new RecordArea(20, RecordArea.FD_FILL);
        RecordArea.Num num = new RecordArea.Num(0, 12, 2, true);
        RecordArea.Alnum text = new RecordArea.Alnum(12, 8);
        assertArrayEquals(new byte[20], area.bytes());

        area.set(text, "ABCDEFGHIJ");
        assertEquals("ABCDEFGH", area.getText(text));
        area.set(text, "AB");
        assertEquals("AB      ", area.getText(text));

        area.initialize(num);
        assertEquals("000000000000", Bytes.ascii(area.bytes(), 0, 12));
        area.set(num, 0);
        assertEquals("00000000000{", Bytes.ascii(area.bytes(), 0, 12));

        RecordArea source = new RecordArea(Bytes.ascii("00000000000}"));
        area.move(num, source, num);
        assertTrue(area.isNegative(num));
        assertEquals("000000000000-", area.display(num));
        source.load(Bytes.ascii("1"));
        assertEquals("1           ", Bytes.ascii(source.bytes(), 0, 12));
        assertFalse(new RecordArea(4).equals(new RecordArea(4, RecordArea.FD_FILL)));
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
