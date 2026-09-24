package carddemo;

import carddemo.cobol.Bytes;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Execution environment of a batch step: DD-name to file mapping, PARM, the job's clock and the
 * SYSOUT stream. {@code DISPLAY} output is written as raw bytes, one record per line, exactly as
 * GnuCOBOL does.
 */
public final class JobContext {
    /** Frozen date the harness runs the COBOL under (faketime 2022-07-18 00:00:00 UTC). */
    public static final String DEFAULT_TIMESTAMP = "2022-07-18-00.00.00.000000";

    private final Map<String, Path> ddNames;
    private final String parm;
    private final String timestamp;
    private final OutputStream sysout;

    public JobContext(Map<String, Path> ddNames, String parm, String timestamp, OutputStream sysout) {
        this.ddNames = Map.copyOf(ddNames);
        this.parm = parm;
        this.timestamp = timestamp;
        this.sysout = sysout;
    }

    /** Path bound to a DD name; a missing DD is a JCL error, so it fails fast. */
    public Path dd(String name) {
        Path path = ddNames.get(name);
        if (path == null) {
            throw new IllegalArgumentException("DD " + name + " is not allocated (use --dd " + name + "=PATH)");
        }
        return path;
    }

    public String parm() {
        return parm;
    }

    /** DB2-style timestamp {@code YYYY-MM-DD-HH.MM.SS.NNNNNN} of "now". */
    public String timestamp() {
        return timestamp;
    }

    public void display(String text) {
        display(Bytes.ascii(text));
    }

    public void display(byte[]... parts) {
        try {
            for (byte[] part : parts) {
                sysout.write(part);
            }
            sysout.write('\n');
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write SYSOUT", e);
        }
    }

    public void display(String label, byte[] value) {
        display(Bytes.ascii(label), value);
    }

    public void display(String label, String value) {
        display(Bytes.ascii(label), Bytes.ascii(value));
    }
}
