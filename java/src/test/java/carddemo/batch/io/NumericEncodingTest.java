package carddemo.batch.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class NumericEncodingTest {

    private static byte[] ascii(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    @Test
    void zonedParsesEbcdicOverpunchSigns() {
        assertEquals(1234, Zoned.parse(ascii("0000000123D"), 0, 11));
        assertEquals(-1230, Zoned.parse(ascii("0000000123}"), 0, 11));
        assertEquals(-1239, Zoned.parse(ascii("0000000123R"), 0, 11));
        assertEquals(0, Zoned.parse(ascii("0000000000{"), 0, 11));
        assertEquals(500, Zoned.parse(ascii("00500"), 0, 5));
    }

    @Test
    void zonedEncodesSignInLastDigit() {
        byte[] buf = new byte[12];
        Zoned.putSigned(buf, 0, 12, 0);
        assertArrayEquals(ascii("00000000000{"), buf);
        Zoned.putSigned(buf, 0, 12, 1235);
        assertArrayEquals(ascii("00000000123E"), buf);
        Zoned.putSigned(buf, 0, 12, -1230);
        assertArrayEquals(ascii("00000000123}"), buf);
        Zoned.putSigned(buf, 0, 12, -7);
        assertArrayEquals(ascii("00000000000P"), buf);
    }

    @Test
    void zonedTruncatesHighOrderDigitsLikeCobol() {
        byte[] buf = new byte[3];
        Zoned.putUnsigned(buf, 0, 3, 12345);
        assertArrayEquals(ascii("345"), buf);
    }

    @Test
    void packedRoundTripsWithTrailingSignNibble() {
        byte[] buf = new byte[Packed.bytesFor(5)];
        Packed.put(buf, 0, 5, 12345);
        assertArrayEquals(new byte[] {0x12, 0x34, 0x5C}, buf);
        assertEquals(12345, Packed.parse(buf, 0, 5));
        Packed.put(buf, 0, 5, -250);
        assertArrayEquals(new byte[] {0x00, 0x25, 0x0D}, buf);
        assertEquals(-250, Packed.parse(buf, 0, 5));
    }
}
