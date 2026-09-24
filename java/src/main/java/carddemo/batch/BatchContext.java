package carddemo.batch;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Job-step environment for a batch program: DD allocations, PARM, clock and SYSOUT. */
public final class BatchContext {
    /** The harness runs the COBOL under a clock frozen at this instant. */
    public static final LocalDateTime FROZEN_NOW = LocalDateTime.of(2022, 7, 18, 0, 0, 0);

    private final Map<String, Path> ddNames;
    private final String parm;
    private final LocalDateTime now;
    private final PrintStream sysout;

    public BatchContext(Map<String, Path> ddNames, String parm, LocalDateTime now, PrintStream sysout) {
        this.ddNames = Map.copyOf(ddNames);
        this.parm = parm;
        this.now = now;
        this.sysout = sysout;
    }

    public Path dd(String name) {
        Path path = ddNames.get(name);
        if (path == null) {
            throw new IllegalArgumentException("Missing --dd " + name + "=<path>");
        }
        return path;
    }

    public String parm() {
        return parm;
    }

    public LocalDateTime now() {
        return now;
    }

    /** DB2-style timestamp built the way the COBOL does from FUNCTION CURRENT-DATE. */
    public String db2Timestamp() {
        int hundredths = now.getNano() / 10_000_000;
        return now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.")) + String.format("%06d", hundredths);
    }

    /** DISPLAY: one line on SYSOUT. */
    public void display(String text) {
        sysout.print(text);
        sysout.print('\n');
    }
}
