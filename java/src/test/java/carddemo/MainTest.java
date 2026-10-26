package carddemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void rejectsUnknownProgramAndMalformedDd() {
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        assertThrows(IllegalArgumentException.class, () -> Main.run(new String[] {}, out));
        assertThrows(IllegalArgumentException.class, () -> Main.run(new String[] {"CBXXXX"}, out));
        assertThrows(IllegalArgumentException.class, () -> Main.run(new String[] {"CBACT02C", "--dd", "CARDFILE"}, out));
        assertThrows(IllegalArgumentException.class, () -> Main.run(new String[] {"CBACT02C", "--parm", "X"}, out));
    }

    @Test
    void missingDdIsReportedBeforeRunning() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[] {"CBACT02C"}, out));
        assertEquals("DD CARDFILE is not allocated (use --dd CARDFILE=PATH)", e.getMessage());
        assertEquals("START OF EXECUTION OF PROGRAM CBACT02C\n", bytes.toString());
    }

    private static Path cardFile() throws IOException {
        Path file = Files.createTempFile("cardfile", ".dat");
        file.toFile().deleteOnExit();
        Files.write(file, "4111111111111111".concat(" ".repeat(134)).getBytes());
        return file;
    }

    @Test
    void lowercaseDdNameIsLocaleIndependent() throws IOException {
        Path file = cardFile();
        Locale saved = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            assertEquals(0, Main.run(new String[] {"cbact02c", "--dd", "cardfile=" + file}, new PrintStream(bytes)));
        } finally {
            Locale.setDefault(saved);
        }
    }

    @Test
    void lostSysoutFailsTheRun() throws IOException {
        Path file = cardFile();
        PrintStream broken = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("disk full");
            }
        });
        assertThrows(Main.SysoutException.class,
                () -> Main.run(new String[] {"CBACT02C", "--dd", "CARDFILE=" + file}, broken));
    }
}
