package carddemo.programs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import carddemo.MainTestAccess;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Runs the Java port on the parity fixtures and compares every output byte-for-byte with the
 * GnuCOBOL outputs captured by {@code scripts/run-parity.sh --update-golden}.
 */
class Cbact01cGoldenTest {
    private static final Path GOLDEN = Path.of("src/test/resources/golden");
    private static final String[] FILES = {"OUTFILE", "ARRYFILE", "VBRCFILE"};

    @TempDir
    Path dir;

    @ParameterizedTest(name = "{0}")
    @CsvSource({
        "acctdata,               ../app/data/ASCII/acctdata.txt,                  OUTFILE",
        "edge-cases,             src/test/resources/fixtures/acct-edge.txt,       OUTFILE",
        "empty-acctfile,         '',                                              OUTFILE",
        "abend-missing-acctfile, -,                                               OUTFILE",
        "abend-outfile-open,     src/test/resources/fixtures/acct-edge.txt,       no-such-dir/OUTFILE"
    })
    void matchesGnuCobol(String scenario, String fixture, String outfile) throws IOException {
        Path acctfile = dir.resolve("ACCTFILE");
        if (fixture == null || fixture.isEmpty()) {
            Files.write(acctfile, new byte[0]);
        } else if (!fixture.equals("-")) {
            Files.write(acctfile, unload(Path.of(fixture)));
        }
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        int rc = MainTestAccess.run(new String[] {"CBACT01C",
            "--dd", "ACCTFILE=" + acctfile,
            "--dd", "OUTFILE=" + dir.resolve(outfile),
            "--dd", "ARRYFILE=" + dir.resolve("ARRYFILE"),
            "--dd", "VBRCFILE=" + dir.resolve("VBRCFILE")},
                Map.of(), new PrintStream(stdout), new PrintStream(new ByteArrayOutputStream()));

        Path expected = GOLDEN.resolve(scenario);
        assertEquals(Files.readString(expected.resolve("RC")).trim(), String.valueOf(rc % 256), "RC");
        assertArrayEquals(Files.readAllBytes(expected.resolve("SYSOUT")), stdout.toByteArray(), "SYSOUT");
        for (String file : FILES) {
            Path golden = expected.resolve(file);
            Path actual = dir.resolve(file);
            if (Files.exists(golden)) {
                assertArrayEquals(Files.readAllBytes(golden), Files.readAllBytes(actual), file);
            } else {
                assertFalse(Files.exists(actual), file + " should not be created");
            }
        }
    }

    /** IDCAMS-REPRO-style unload of a line-sequential fixture: 300-byte records, space padded. */
    private static byte[] unload(Path fixture) throws IOException {
        List<String> lines = new ArrayList<>(Files.readAllLines(fixture, StandardCharsets.US_ASCII));
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(String.format("%-300s", line));
        }
        return sb.toString().getBytes(StandardCharsets.US_ASCII);
    }
}
