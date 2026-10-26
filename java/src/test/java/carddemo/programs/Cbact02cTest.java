package carddemo.programs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import carddemo.JobContext;
import carddemo.records.CardRecord;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Cbact02cTest {
    /** Line-delimited ASCII rendering of AWS.M2.CARDDEMO.CARDDATA (50 x 150-byte records). */
    private static final Path FIXTURE = Path.of("..", "app", "data", "ASCII", "carddata.txt");

    @TempDir
    Path tmp;

    private static List<byte[]> fixtureRecords() throws IOException {
        List<byte[]> records = new ArrayList<>();
        for (String line : Files.readAllLines(FIXTURE, StandardCharsets.ISO_8859_1)) {
            byte[] record = line.getBytes(StandardCharsets.ISO_8859_1);
            assertEquals(CardRecord.LENGTH, record.length);
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

    private static byte[] expectedSysout(List<byte[]> records) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write((Cbact02c.START_MESSAGE + "\n").getBytes(StandardCharsets.US_ASCII));
        for (byte[] record : records) {
            out.write(record);
            out.write('\n');
        }
        out.write((Cbact02c.END_MESSAGE + "\n").getBytes(StandardCharsets.US_ASCII));
        return out.toByteArray();
    }

    private static int run(Path cardFile, ByteArrayOutputStream sysout) {
        return new Cbact02c().run(new JobContext(Map.of("CARDFILE", cardFile), sysout));
    }

    @Test
    void printsEveryCardRecordInKeyOrderAndReturnsZero() throws IOException {
        List<byte[]> records = fixtureRecords();
        assertEquals(50, records.size());
        Path cardFile = writeFixed(tmp.resolve("CARDFILE"), records);

        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertEquals(0, run(cardFile, sysout));
        assertArrayEquals(expectedSysout(records), sysout.toByteArray());
    }

    @Test
    void ksdsIsReadInPrimaryKeyOrderRegardlessOfLoadOrder() throws IOException {
        List<byte[]> records = fixtureRecords();
        List<byte[]> shuffled = new ArrayList<>(records);
        Collections.reverse(shuffled);
        Path cardFile = writeFixed(tmp.resolve("CARDFILE"), shuffled);

        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertEquals(0, run(cardFile, sysout));
        assertArrayEquals(expectedSysout(records), sysout.toByteArray());
    }

    @Test
    void emptyFilePrintsOnlyStartAndEnd() throws IOException {
        Path cardFile = writeFixed(tmp.resolve("CARDFILE"), List.of());
        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertEquals(0, run(cardFile, sysout));
        assertEquals(Cbact02c.START_MESSAGE + "\n" + Cbact02c.END_MESSAGE + "\n",
                sysout.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void missingCardFileAbendsWithStatus35() {
        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertThrows(Abend.class, () -> run(tmp.resolve("does-not-exist"), sysout));
        assertEquals(String.join("\n",
                Cbact02c.START_MESSAGE,
                "ERROR OPENING CARDFILE",
                "FILE STATUS IS: NNNN0035",
                "ABENDING PROGRAM") + "\n",
                sysout.toString(StandardCharsets.US_ASCII));
    }

    @Test
    void corruptCardFileAbendsWithStatus30() throws IOException {
        Path cardFile = tmp.resolve("CARDFILE");
        Files.write(cardFile, new byte[] {1, 2, 3});
        ByteArrayOutputStream sysout = new ByteArrayOutputStream();
        assertThrows(Abend.class, () -> run(cardFile, sysout));
        assertEquals(String.join("\n",
                Cbact02c.START_MESSAGE,
                "ERROR OPENING CARDFILE",
                "FILE STATUS IS: NNNN0030",
                "ABENDING PROGRAM") + "\n",
                sysout.toString(StandardCharsets.US_ASCII));
    }
}
