package carddemo;

import carddemo.cobol.Bytes;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Execution environment of a batch step: DD-name to file mapping and the SYSOUT stream.
 * {@code DISPLAY} output is written as raw bytes, one record per line, exactly as GnuCOBOL does.
 */
public final class JobContext {
    private final Map<String, Path> ddNames;
    private final OutputStream sysout;

    public JobContext(Map<String, Path> ddNames, OutputStream sysout) {
        this.ddNames = Map.copyOf(ddNames);
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

    public void display(String label, String value) {
        display(Bytes.ascii(label), Bytes.ascii(value));
    }
}
