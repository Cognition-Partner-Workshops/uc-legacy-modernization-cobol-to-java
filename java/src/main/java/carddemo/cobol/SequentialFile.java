package carddemo.cobol;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * {@code ORGANIZATION IS SEQUENTIAL} output files as GnuCOBOL writes them: fixed records back to
 * back with no delimiters, or (RECFM=V) each record prefixed by a 4-byte header in
 * {@code COB_VARSEQ_FORMAT=0}: 2-byte big-endian data length followed by two NUL bytes.
 */
public final class SequentialFile implements AutoCloseable {
    private final Path path;
    private final boolean variable;
    private final OutputStream out;

    private SequentialFile(Path path, boolean variable, OutputStream out) {
        this.path = path;
        this.variable = variable;
        this.out = out;
    }

    /** {@code OPEN OUTPUT} of a fixed-length (RECFM=F/FB) file. */
    public static SequentialFile openFixed(Path path) throws FileStatusException {
        return open(path, false);
    }

    /** {@code OPEN OUTPUT} of a variable-length (RECFM=V/VB) file. */
    public static SequentialFile openVariable(Path path) throws FileStatusException {
        return open(path, true);
    }

    private static SequentialFile open(Path path, boolean variable) throws FileStatusException {
        if (Files.isDirectory(path)) {
            throw new FileStatusException("37", path + " is a directory", null);
        }
        try {
            return new SequentialFile(path, variable, Files.newOutputStream(path));
        } catch (NoSuchFileException e) {
            throw new FileStatusException("30", "cannot create " + path, e);
        } catch (AccessDeniedException e) {
            throw new FileStatusException("37", "permission denied on " + path, e);
        } catch (IOException e) {
            throw new FileStatusException("30", "cannot open " + path, e);
        }
    }

    public void write(byte[] record) throws FileStatusException {
        try {
            if (variable) {
                out.write(new byte[] {(byte) (record.length >> 8), (byte) record.length, 0, 0});
            }
            out.write(record);
        } catch (IOException e) {
            throw new FileStatusException("30", "cannot write " + path, e);
        }
    }

    @Override
    public void close() throws FileStatusException {
        try {
            out.close();
        } catch (IOException e) {
            throw new FileStatusException("30", "cannot close " + path, e);
        }
    }
}
