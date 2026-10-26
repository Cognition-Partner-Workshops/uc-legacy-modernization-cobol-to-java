package carddemo.programs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import carddemo.JobContext;
import carddemo.cobol.Bytes;
import carddemo.cobol.PackedDecimal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Cbact01cTest {
    @TempDir
    Path dir;

    private static String account(String id, String status, String bal, String limit, String reissue,
            String debit, String group) {
        String rec = id + status + bal + limit + "00000001000{" + "2010-01-01" + "2030-12-31" + reissue
                + "00000000100{" + debit + "12345     " + group;
        return String.format("%-300s", rec);
    }

    private ByteArrayOutputStream sysout;

    private int run(String... accounts) throws IOException {
        Files.writeString(dir.resolve("ACCTFILE"), String.join("", accounts), StandardCharsets.US_ASCII);
        sysout = new ByteArrayOutputStream();
        Map<String, Path> dds = Map.of(
                "ACCTFILE", dir.resolve("ACCTFILE"),
                "OUTFILE", dir.resolve("OUTFILE"),
                "ARRYFILE", dir.resolve("ARRYFILE"),
                "VBRCFILE", dir.resolve("VBRCFILE"));
        return new Cbact01c().run(new JobContext(dds, sysout));
    }

    private byte[] read(String dd) throws IOException {
        return Files.readAllBytes(dir.resolve(dd));
    }

    @Test
    void outRecordLayoutAndDateConversion() throws IOException {
        assertEquals(0, run(account("00000000001", "Y", "00000019400{", "00000020200{", "2025-05-20",
                "00000000000{", "A000000000")));
        byte[] out = read("OUTFILE");
        assertEquals(107, out.length);
        assertEquals("00000000001Y00000019400{00000020200{00000001000{2010-01-012030-12-3120250520  00000000100{",
                Bytes.ascii(out, 0, 90));
        assertEquals(new BigDecimal("2525.00"), PackedDecimal.decode(out, 90, 12, 2));
        assertEquals("A000000000", Bytes.ascii(out, 97, 10));
    }

    @Test
    void outDebitIsOnlySetWhenAccountDebitIsZero() throws IOException {
        // Account 2 has a non-zero debit, so OUT-ACCT-CURR-CYC-DEBIT keeps account 1's 2525.00;
        // with a non-zero debit on the first record the FD's initial binary zeros are written.
        run(account("00000000001", "Y", "00000000000{", "00000000000{", "2025-05-20", "00000000000{", "G1"),
                account("00000000002", "Y", "00000000000{", "00000000000{", "2025-05-20", "00000005000{", "G2"));
        byte[] out = read("OUTFILE");
        assertEquals(new BigDecimal("2525.00"), PackedDecimal.decode(out, 107 + 90, 12, 2));

        run(account("00000000003", "Y", "00000000000{", "00000000000{", "2025-05-20", "00000005000{", "G3"));
        assertArrayEquals(new byte[7], Bytes.slice(read("OUTFILE"), 90, 7));
    }

    @Test
    void negativeZeroDebitCountsAsZero() throws IOException {
        run(account("00000000004", "N", "00000000000}", "00000000000{", "2025-05-20", "00000000000}", "G4"));
        assertEquals(new BigDecimal("2525.00"), PackedDecimal.decode(read("OUTFILE"), 90, 12, 2));
        String display = sysout.toString(StandardCharsets.US_ASCII);
        assertTrue(display.contains("ACCT-CURR-BAL           :000000000000-\n"), display);
    }

    @Test
    void arrayRecordSlots() throws IOException {
        run(account("00000000005", "Y", "00000010253P", "00000000000{", "2025-05-20", "00000000000{", "G5"));
        byte[] arr = read("ARRYFILE");
        assertEquals(110, arr.length);
        assertEquals("00000000005", Bytes.ascii(arr, 0, 11));
        assertEquals("00000010253P", Bytes.ascii(arr, 11, 12));
        assertEquals(new BigDecimal("1005.00"), PackedDecimal.decode(arr, 23, 12, 2));
        assertEquals("00000010253P", Bytes.ascii(arr, 30, 12));
        assertEquals(new BigDecimal("1525.00"), PackedDecimal.decode(arr, 42, 12, 2));
        assertEquals("00000010250}", Bytes.ascii(arr, 49, 12));
        assertEquals(new BigDecimal("-2500.00"), PackedDecimal.decode(arr, 61, 12, 2));
        // Slots 4 and 5 keep INITIALIZE values: unsigned ASCII zeros and packed +0; ARR-FILLER spaces.
        assertEquals("000000000000", Bytes.ascii(arr, 68, 12));
        assertEquals(BigDecimal.ZERO.setScale(2), PackedDecimal.decode(arr, 80, 12, 2));
        assertEquals("000000000000", Bytes.ascii(arr, 87, 12));
        assertEquals("    ", Bytes.ascii(arr, 106, 4));
    }

    @Test
    void variableRecordsAre12And39Bytes() throws IOException {
        run(account("00000000006", "Y", "00000000420{", "00000003000{", "2027-01-01", "00000000000{", "G6"));
        byte[] vb = read("VBRCFILE");
        assertEquals(4 + 12 + 4 + 39, vb.length);
        assertArrayEquals(new byte[] {0, 12, 0, 0}, Bytes.slice(vb, 0, 4));
        assertEquals("00000000006Y", Bytes.ascii(vb, 4, 12));
        assertArrayEquals(new byte[] {0, 39, 0, 0}, Bytes.slice(vb, 16, 4));
        assertEquals("0000000000600000000420{00000003000{2027", Bytes.ascii(vb, 20, 39));
    }

    @Test
    void accountsAreReadInKeyOrder() throws IOException {
        run(account("00000000020", "Y", "00000000000{", "00000000000{", "2025-05-20", "00000000000{", "B"),
                account("00000000010", "Y", "00000000000{", "00000000000{", "2025-05-20", "00000000000{", "A"));
        byte[] out = read("OUTFILE");
        assertEquals("00000000010", Bytes.ascii(out, 0, 11));
        assertEquals("00000000020", Bytes.ascii(out, 107, 11));
    }

    @Test
    void missingAccountFileAbendsWith999() {
        sysout = new ByteArrayOutputStream();
        JobContext job = new JobContext(Map.of(
                "ACCTFILE", dir.resolve("none"), "OUTFILE", dir.resolve("o"),
                "ARRYFILE", dir.resolve("a"), "VBRCFILE", dir.resolve("v")), sysout);
        Abend abend = assertThrows(Abend.class, () -> new Cbact01c().run(job));
        assertEquals(999, abend.abendCode());
        assertEquals("""
                START OF EXECUTION OF PROGRAM CBACT01C
                ERROR OPENING ACCTFILE
                FILE STATUS IS: NNNN0035
                ABENDING PROGRAM
                """, sysout.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void instancesAreSingleUse() throws IOException {
        Files.write(dir.resolve("ACCTFILE"), new byte[0]);
        Map<String, Path> dds = Map.of("ACCTFILE", dir.resolve("ACCTFILE"), "OUTFILE", dir.resolve("o"),
                "ARRYFILE", dir.resolve("a"), "VBRCFILE", dir.resolve("v"));
        Cbact01c program = new Cbact01c();
        assertEquals(0, program.run(new JobContext(dds, new ByteArrayOutputStream())));
        assertThrows(IllegalStateException.class,
                () -> program.run(new JobContext(dds, new ByteArrayOutputStream())));
    }

    @Test
    void unexpectedFailureStillClosesOpenedOutputs() throws IOException {
        Files.write(dir.resolve("ACCTFILE"), new byte[0]);
        Map<String, Path> dds = Map.of("ACCTFILE", dir.resolve("ACCTFILE"), "OUTFILE", dir.resolve("o"));
        assertThrows(IllegalArgumentException.class,
                () -> new Cbact01c().run(new JobContext(dds, new ByteArrayOutputStream())));
        assertTrue(Files.exists(dir.resolve("o")));
        Files.delete(dir.resolve("o"));
    }

    @Test
    void ioStatusFormatting() {
        assertEquals("0035", Cbact01c.ioStatus04("35"));
        assertEquals("9034", Cbact01c.ioStatus04("9\""));
        assertEquals("A066", Cbact01c.ioStatus04("AB"));
    }
}
