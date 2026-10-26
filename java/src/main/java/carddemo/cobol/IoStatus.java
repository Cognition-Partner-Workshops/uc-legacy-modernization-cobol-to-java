package carddemo.cobol;

/**
 * The CardDemo batch programs' {@code 9910-DISPLAY-IO-STATUS} paragraph: formats a two-byte
 * COBOL file status as the four characters printed after {@code FILE STATUS IS: NNNN}.
 */
public final class IoStatus {
    private IoStatus() {
    }

    /**
     * A numeric status other than {@code 9x} is printed as {@code 00} + status. A {@code 9x}
     * status or one containing a non-digit byte is printed as the first byte followed by the
     * binary value of the second byte as three digits (the COBOL moves it through a
     * {@code PIC 9(4) BINARY} redefinition).
     */
    public static String displayText(byte[] status) {
        if (status.length != 2) {
            throw new IllegalArgumentException("file status must be two bytes");
        }
        boolean numeric = isDigit(status[0]) && isDigit(status[1]);
        if (!numeric || status[0] == '9') {
            int second = status[1] & 0xFF;
            return (char) (status[0] & 0xFF) + ZonedDecimal.leftPad(Integer.toString(second % 1000), 3);
        }
        return "00" + Bytes.ascii(status, 0, 2);
    }

    public static String displayText(String status) {
        return displayText(Bytes.ascii(status));
    }

    private static boolean isDigit(byte b) {
        return b >= '0' && b <= '9';
    }
}
