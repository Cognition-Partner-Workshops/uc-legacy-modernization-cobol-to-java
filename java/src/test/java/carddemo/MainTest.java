package carddemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Main.run(new String[] {"CBACT02C"}, out));
        assertEquals("DD CARDFILE is not allocated (use --dd CARDFILE=PATH)", e.getMessage());
    }
}
