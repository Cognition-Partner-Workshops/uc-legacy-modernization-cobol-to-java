import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Java migration of the CardDemo batch program CBACT01C.cbl.
 *
 * Reads the account master file (fixed 300-byte records laid out per the
 * CVACT01Y copybook), displays every record, and writes three output files:
 * an account extract (OUTFILE, includes a COMP-3 packed-decimal field), an
 * array extract (ARRYFILE, zoned + COMP-3 pairs), and a variable-length
 * record file (VBRCFILE).
 *
 * Input may be EBCDIC (IBM037, the mainframe format) or ASCII. Output text
 * fields are written in ASCII with EBCDIC-style sign overpunch so results are
 * directly comparable with a GnuCOBOL (-fsign=EBCDIC) reference run.
 *
 * Usage: java CBACT01C <acctfile> <outdir> [ebcdic|ascii]
 */
public final class CBACT01C {

    private static final int RECORD_LENGTH = 300;
    private static final String EBCDIC = "IBM037";

    // CVACT01Y field offsets within the 300-byte ACCOUNT-RECORD
    private static final int ACCT_ID_OFF = 0,   ACCT_ID_LEN = 11;      // PIC 9(11)
    private static final int STATUS_OFF = 11;                          // PIC X(01)
    private static final int CURR_BAL_OFF = 12;                        // PIC S9(10)V99
    private static final int CREDIT_LIMIT_OFF = 24;
    private static final int CASH_CREDIT_LIMIT_OFF = 36;
    private static final int OPEN_DATE_OFF = 48;                       // PIC X(10)
    private static final int EXPIRATION_DATE_OFF = 58;
    private static final int REISSUE_DATE_OFF = 68;
    private static final int CYC_CREDIT_OFF = 78;                      // PIC S9(10)V99
    private static final int CYC_DEBIT_OFF = 90;
    private static final int GROUP_ID_OFF = 112, GROUP_ID_LEN = 10;    // PIC X(10)
    private static final int SIGNED_LEN = 12;                          // S9(10)V99 zoned

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java CBACT01C <acctfile> <outdir> [ebcdic|ascii]");
            System.exit(2);
        }
        Path acctFile = Paths.get(args[0]);
        Path outDir = Paths.get(args[1]);
        String charset = (args.length > 2 && args[2].equalsIgnoreCase("ascii"))
                ? "ISO-8859-1" : EBCDIC;
        Files.createDirectories(outDir);

        byte[] data = Files.readAllBytes(acctFile);
        if (data.length % RECORD_LENGTH != 0) {
            System.err.println("ERROR OPENING ACCTFILE");
            System.exit(12);
        }

        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        try (OutputStream outFile = Files.newOutputStream(outDir.resolve("OUTFILE"));
             OutputStream arrFile = Files.newOutputStream(outDir.resolve("ARRYFILE"));
             OutputStream vbrFile = Files.newOutputStream(outDir.resolve("VBRCFILE"))) {

            BigDecimal outCycDebit = BigDecimal.ZERO; // persists across records like the FD field

            for (int off = 0; off < data.length; off += RECORD_LENGTH) {
                String rec = new String(data, off, RECORD_LENGTH, charset);

                String acctId = rec.substring(ACCT_ID_OFF, ACCT_ID_OFF + ACCT_ID_LEN);
                String activeStatus = rec.substring(STATUS_OFF, STATUS_OFF + 1);
                String currBal = zoned(rec, CURR_BAL_OFF);
                String creditLimit = zoned(rec, CREDIT_LIMIT_OFF);
                String cashCreditLimit = zoned(rec, CASH_CREDIT_LIMIT_OFF);
                String openDate = rec.substring(OPEN_DATE_OFF, OPEN_DATE_OFF + 10);
                String expirationDate = rec.substring(EXPIRATION_DATE_OFF, EXPIRATION_DATE_OFF + 10);
                String reissueDate = rec.substring(REISSUE_DATE_OFF, REISSUE_DATE_OFF + 10);
                String cycCredit = zoned(rec, CYC_CREDIT_OFF);
                String cycDebit = zoned(rec, CYC_DEBIT_OFF);
                String groupId = rec.substring(GROUP_ID_OFF, GROUP_ID_OFF + GROUP_ID_LEN);

                // 1100-DISPLAY-ACCT-RECORD
                System.out.println("ACCT-ID                 :" + acctId);
                System.out.println("ACCT-ACTIVE-STATUS      :" + activeStatus);
                System.out.println("ACCT-CURR-BAL           :" + edited(currBal));
                System.out.println("ACCT-CREDIT-LIMIT       :" + edited(creditLimit));
                System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + edited(cashCreditLimit));
                System.out.println("ACCT-OPEN-DATE          :" + openDate);
                System.out.println("ACCT-EXPIRAION-DATE     :" + expirationDate);
                System.out.println("ACCT-REISSUE-DATE       :" + reissueDate);
                System.out.println("ACCT-CURR-CYC-CREDIT    :" + edited(cycCredit));
                System.out.println("ACCT-CURR-CYC-DEBIT     :" + edited(cycDebit));
                System.out.println("ACCT-GROUP-ID           :" + groupId);
                System.out.println("-------------------------------------------------");

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                // COBDATFT: YYYY-MM-DD -> YYYYMMDD (OUT-ACCT-REISSUE-DATE PIC X(10))
                String reissueOut = (reissueDate.substring(0, 4) + reissueDate.substring(5, 7)
                        + reissueDate.substring(8, 10) + "  ");
                if (zonedValue(cycDebit).signum() == 0) {
                    outCycDebit = new BigDecimal("2525.00");
                }
                ByteArrayOutputStream outRec = new ByteArrayOutputStream();
                outRec.write(ascii(acctId));
                outRec.write(ascii(activeStatus));
                outRec.write(ascii(currBal));
                outRec.write(ascii(creditLimit));
                outRec.write(ascii(cashCreditLimit));
                outRec.write(ascii(openDate));
                outRec.write(ascii(expirationDate));
                outRec.write(ascii(reissueOut));
                outRec.write(ascii(cycCredit));
                outRec.write(packComp3(outCycDebit));
                outRec.write(ascii(groupId));
                outFile.write(outRec.toByteArray());

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                BigDecimal bal = zonedValue(currBal);
                ByteArrayOutputStream arrRec = new ByteArrayOutputStream();
                arrRec.write(ascii(acctId));
                arrRec.write(arrayElement(bal, new BigDecimal("1005.00")));
                arrRec.write(arrayElement(bal, new BigDecimal("1525.00")));
                arrRec.write(arrayElement(new BigDecimal("-1025.00"), new BigDecimal("-2500.00")));
                arrRec.write(initializedElement());
                arrRec.write(initializedElement());
                arrRec.write(ascii("    "));
                arrFile.write(arrRec.toByteArray());

                // 1500-POPUL-VBRC-RECORD
                String vbrcRec1 = acctId + activeStatus;
                String vbrcRec2 = acctId + currBal + creditLimit + reissueDate.substring(0, 4);
                System.out.println("VBRC-REC1:" + vbrcRec1);
                System.out.println("VBRC-REC2:" + vbrcRec2);

                // 1550/1575-WRITE-VBx-RECORD (variable-length records)
                writeVariable(vbrFile, ascii(vbrcRec1));
                writeVariable(vbrFile, ascii(vbrcRec2));

                // Main loop: DISPLAY ACCOUNT-RECORD
                System.out.println(rec);
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    /** DISPLAY rendering of a zoned S9(10)V99 field: sign, digits, decimal point. */
    private static String edited(String zoned) {
        long cents = zonedValue(zoned).movePointRight(2).longValueExact();
        return String.format("%s%010d.%02d",
                cents < 0 ? "-" : "+", Math.abs(cents) / 100, Math.abs(cents) % 100);
    }

    /** Raw 12-character zoned-decimal field (sign overpunch in last byte). */
    private static String zoned(String rec, int off) {
        return rec.substring(off, off + SIGNED_LEN);
    }

    /** Decode a zoned decimal S9(10)V99 string (EBCDIC-style overpunch) to a value. */
    private static BigDecimal zonedValue(String zoned) {
        char last = zoned.charAt(zoned.length() - 1);
        int digit;
        boolean negative = false;
        if (last == '{') {
            digit = 0;
        } else if (last == '}') {
            digit = 0;
            negative = true;
        } else if (last >= 'A' && last <= 'I') {
            digit = last - 'A' + 1;
        } else if (last >= 'J' && last <= 'R') {
            digit = last - 'J' + 1;
            negative = true;
        } else if (Character.isDigit(last)) {
            digit = last - '0';
        } else {
            throw new IllegalArgumentException("Bad zoned decimal: " + zoned);
        }
        String digits = zoned.substring(0, zoned.length() - 1) + digit;
        BigDecimal value = new BigDecimal(digits).movePointLeft(2);
        return negative ? value.negate() : value;
    }

    /** Encode a value as zoned decimal S9(10)V99 with EBCDIC-style sign overpunch. */
    private static String zonedEncode(BigDecimal value) {
        boolean negative = value.signum() < 0;
        String digits = value.abs().movePointRight(2).setScale(0).toPlainString();
        digits = "000000000000".substring(digits.length()) + digits;
        int lastDigit = digits.charAt(11) - '0';
        char overpunch;
        if (negative) {
            overpunch = lastDigit == 0 ? '}' : (char) ('J' + lastDigit - 1);
        } else {
            overpunch = lastDigit == 0 ? '{' : (char) ('A' + lastDigit - 1);
        }
        return digits.substring(0, 11) + overpunch;
    }

    /** Encode a value as COMP-3 packed decimal PIC S9(10)V99 (7 bytes, 13 nibbles incl. sign). */
    private static byte[] packComp3(BigDecimal value) {
        boolean negative = value.signum() < 0;
        String digits = value.abs().movePointRight(2).setScale(0).toPlainString();
        digits = "000000000000".substring(digits.length()) + digits; // 12 digits
        byte[] packed = new byte[7];
        String nibbles = "0" + digits; // 13 digit nibbles + sign nibble
        for (int i = 0; i < 6; i++) {
            packed[i] = (byte) (((nibbles.charAt(2 * i) - '0') << 4)
                    | (nibbles.charAt(2 * i + 1) - '0'));
        }
        packed[6] = (byte) (((nibbles.charAt(12) - '0') << 4) | (negative ? 0x0D : 0x0C));
        return packed;
    }

    /** An INITIALIZE-d ARR-ACCT-BAL element: unpunched zoned zeros + COMP-3 zero. */
    private static byte[] initializedElement() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ascii("000000000000"));
        out.write(packComp3(BigDecimal.ZERO));
        return out.toByteArray();
    }

    /** One ARR-ACCT-BAL element: zoned curr-bal + COMP-3 cyc-debit. */
    private static byte[] arrayElement(BigDecimal currBal, BigDecimal cycDebit) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(ascii(zonedEncode(currBal)));
        out.write(packComp3(cycDebit));
        return out.toByteArray();
    }

    /** GnuCOBOL varseq format 0: 2-byte big-endian length + 2 NUL bytes + record data. */
    private static void writeVariable(OutputStream out, byte[] record) throws IOException {
        out.write((record.length >> 8) & 0xFF);
        out.write(record.length & 0xFF);
        out.write(0);
        out.write(0);
        out.write(record);
    }

    private static byte[] ascii(String s) {
        return s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    private CBACT01C() {}
}
