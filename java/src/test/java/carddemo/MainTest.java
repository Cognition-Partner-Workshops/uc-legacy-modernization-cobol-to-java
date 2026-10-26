package carddemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
    @TempDir
    Path dir;

    @Test
    void ddNamesFromEnvironmentAndAbendExitCode() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int rc = Main.run(new String[] {"cbact01c"},
                Map.of("DD_ACCTFILE", dir.resolve("none").toString(), "DD_OUTFILE", dir.resolve("o").toString(),
                        "DD_ARRYFILE", dir.resolve("a").toString(), "DD_VBRCFILE", dir.resolve("v").toString()),
                new PrintStream(out), new PrintStream(new ByteArrayOutputStream()));
        assertEquals(999, rc);
        assertTrue(out.toString(StandardCharsets.US_ASCII).endsWith("ABENDING PROGRAM\n"));
    }

    @Test
    void emptyAccountFileEndsNormally() throws Exception {
        Files.write(dir.resolve("acct"), new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int rc = Main.run(new String[] {"CBACT01C", "--dd", "ACCTFILE=" + dir.resolve("acct"),
            "--dd", "OUTFILE=" + dir.resolve("o"), "--dd", "ARRYFILE=" + dir.resolve("a"),
            "--dd", "VBRCFILE=" + dir.resolve("v")}, Map.of(), new PrintStream(out),
                new PrintStream(new ByteArrayOutputStream()));
        assertEquals(0, rc);
        assertEquals("START OF EXECUTION OF PROGRAM CBACT01C\nEND OF EXECUTION OF PROGRAM CBACT01C\n",
                out.toString(StandardCharsets.US_ASCII));
        assertEquals(0, Files.size(dir.resolve("o")));
    }

    @Test
    void usageErrors() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream stderr = new PrintStream(err);
        PrintStream stdout = new PrintStream(new ByteArrayOutputStream());
        assertEquals(2, Main.run(new String[0], Map.of(), stdout, stderr));
        assertEquals(2, Main.run(new String[] {"NOPE"}, Map.of(), stdout, stderr));
        assertEquals(2, Main.run(new String[] {"CBACT01C", "--dd", "BAD"}, Map.of(), stdout, stderr));
        assertTrue(err.toString(StandardCharsets.US_ASCII).contains("usage:"));
    }
}
