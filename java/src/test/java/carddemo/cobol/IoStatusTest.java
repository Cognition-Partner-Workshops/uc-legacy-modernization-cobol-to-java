package carddemo.cobol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IoStatusTest {

    @Test
    void numericStatusIsZeroPaddedToFourDigits() {
        assertEquals("0035", IoStatus.displayText("35"));
        assertEquals("0010", IoStatus.displayText("10"));
        assertEquals("0000", IoStatus.displayText("00"));
    }

    @Test
    void nineSeriesStatusPrintsBinaryValueOfSecondByte() {
        // GnuCOBOL/IBM extended statuses: first byte '9', second byte a binary code.
        assertEquals("9005", IoStatus.displayText(new byte[] {'9', 0x05}));
        assertEquals("9065", IoStatus.displayText(new byte[] {'9', 'A'}));
        assertEquals("9255", IoStatus.displayText(new byte[] {'9', (byte) 0xFF}));
    }

    @Test
    void nonNumericStatusTakesTheBinaryPath() {
        assertEquals("3065", IoStatus.displayText(new byte[] {'3', 'A'}));
    }
}
