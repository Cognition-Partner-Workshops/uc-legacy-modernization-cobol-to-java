package carddemo.programs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import carddemo.JobContext;
import carddemo.records.CardXrefRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Cbact03cTest {
    /** EBCDIC (CP037) unload of AWS.M2.CARDDEMO.CARDXREF: 50 x 50-byte records, no delimiters. */
    private static final Path EBCDIC_FIXTURE = Path.of("..", "app", "data", "EBCDIC", "AWS.M2.CARDDEMO.CARDXREF.PS");
    /** Line-delimited ASCII rendering of the same data with trailing spaces removed. */
    private static final Path ASCII_FIXTURE = Path.of("..", "app", "data", "ASCII", "cardxref.txt");
    private static final Charset CP037 = Charset.forName("IBM037");

    @TempDir
    Path tmp;

    /** The EBCDIC extract converted to ASCII, split into 50-byte records. */
    private static List<byte[]> fixtureRecords() throws IOException {
        byte[] ebcdic = Files.readAllBytes(EBCDIC_FIXTURE);
        assertEquals(0, ebcdic.length % CardXrefRecord.LENGTH);
        byte[] ascii = new String(ebcdic, CP037).getBytes(StandardCharsets.ISO_8859_1);
        assertEquals(ebcdic.length, ascii.length);
        List<byte[]> records = new ArrayList<>();
        for (int off = 0; off < ascii.length; off += CardXrefRecord.LENGTH) {
            byte[] record = new byte[CardXrefRecord.LENGTH];
            System.arraycopy(ascii, off, record, 0, CardXrefRecord.LENGTH);
            records.add(record);
        }
        return records;
    }

    private static Path writeFixed(Path path, List<byte[]> records) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] record : records) {
            out.write(record);
        }
        Files.write(path, out.toByteArray());
        return path;
    }

    /** CBACT03C displays every record twice: once in 1000-XREFFILE-GET-NEXT and once in the main loop. */
    private static byte[] expectedSysout(List<byte[]> records) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write((Cbact03c.START_MESSAGE + "\n").getBytes(StandardCharsets.US_ASCII));
        for (byte[] record : records) {
            out.write(record);
            out.write('\n');
            out.write(record);
            out.write('\n');
        }
        out.write((Cbact03c.END_MESSAGE + "\n").getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    private static int run(Path xrefFile, ByteArrayOutputStream sysout) {
        return new Cbact03c().run(new JobContext(Map.of("XREFFILE", xrefFile), sysout));
    }

    @Test
    void ebcdicFixtureMatchesAsciiFixtureAfterCodePageNormalisation() throws IOException {
        List<byte[]> records = fixtureRecords();
        List<String> asciiLines = Files.readAllLines(ASCII_FIXTURE, StandardCharsets.ISO_8859_1);
        assertEquals(asciiLines.size(), records.size());
        for (int i = 0; i < records.size(); i++) {
            String record = new String(records.get(i), StandardCharsets.ISO_8859_1);
            assertEquals(asciiLines.get(i).stripTrailing(), record.stripTrailing(), "record " + (i + 1));
        }
    }

    @Test
    void printsEveryXrefRecordTwiceInKeyOrderAndReturnsZero() throws IOException {
        List<byte[]> records = fixtureRecords();
        assertEquals(50, records.size());
        Path xrefFile = writeFixed(tmp.resolve("XREFFILE"), records);

        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertEquals(0, run(xrefFile, sysout));
        assertArrayEquals(expectedSysout(records), sysout.toByteArray());
        assertEquals(2 + 2 * 50, sysout.toString(StandardCharsets.US_ASCII).split("\n").length);
    }

    @Test
    void ksdsIsReadInPrimaryKeyOrderRegardlessOfLoadOrder() throws IOException {
        List<byte[]> records = fixtureRecords();
        List<byte[]> shuffled = new ArrayList<>(records);
        Collections.reverse(shuffled);
        Path xrefFile = writeFixed(tmp.resolve("XREFFILE"), shuffled);

        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertEquals(0, run(xrefFile, sysout));
        assertArrayEquals(expectedSysout(records), sysout.toByteArray());
    }

    @Test
    void emptyFilePrintsOnlyStartAndEnd() throws IOException {
        Path xrefFile = writeFixed(tmp.resolve("XREFFILE"), List.of());
        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertEquals(0, run(xrefFile, sysout));
        assertEquals(Cbact03c.START_MESSAGE + "\n" + Cbact03c.END_MESSAGE + "\n",
                sysout.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void missingXrefFileAbendsWithStatus35() {
        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertThrows(Abend.class, () -> run(tmp.resolve("does-not-exist"), sysout));
        assertEquals(String.join("\n",
                Cbact03c.START_MESSAGE,
                "ERROR OPENING XREFFILE",
                "FILE STATUS IS: NNNN0035",
                "ABENDING PROGRAM") + "\n",
                sysout.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void corruptXrefFileAbendsWithStatus30() throws IOException {
        Path xrefFile = tmp.resolve("XREFFILE");
        Files.write(xrefFile, new byte[] {1, 2, 3});
        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertThrows(Abend.class, () -> run(xrefFile, sysout));
        assertEquals(String.join("\n",
                Cbact03c.START_MESSAGE,
                "ERROR OPENING XREFFILE",
                "FILE STATUS IS: NNNN0030",
                "ABENDING PROGRAM") + "\n",
                sysout.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void duplicateKeyInUnloadAbendsWithStatus30() throws IOException {
        List<byte[]> records = fixtureRecords();
        Path xrefFile = writeFixed(tmp.resolve("XREFFILE"), List.of(records.get(0), records.get(0)));
        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertThrows(Abend.class, () -> run(xrefFile, sysout));
        assertEquals(String.join("\n",
                Cbact03c.START_MESSAGE,
                "ERROR OPENING XREFFILE",
                "FILE STATUS IS: NNNN0030",
                "ABENDING PROGRAM") + "\n",
                sysout.toString(StandardCharsets.US_ASCII));
    }
}
