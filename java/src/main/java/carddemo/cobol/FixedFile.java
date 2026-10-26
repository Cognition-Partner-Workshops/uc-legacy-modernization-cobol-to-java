package carddemo.cobol;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Fixed-length record sequential files (GnuCOBOL {@code ORGANIZATION IS SEQUENTIAL}, no record delimiters). */
public final class FixedFile {
    private FixedFile() {
    }

    /** Reads every record; propagates the {@link IOException} so callers can map it to a COBOL file status. */
    public static List<byte[]> readAll(Path path, int recordLength) throws IOException {
        byte[] data = Files.readAllBytes(path);
        if (data.length % recordLength != 0) {
            throw new IOException(
                    path + ": size " + data.length + " is not a multiple of record length " + recordLength);
        }
        List<byte[]> records = new ArrayList<>(data.length / recordLength);
        for (int i = 0; i < data.length; i += recordLength) {
            records.add(Bytes.slice(data, i, recordLength));
        }
        return records;
    }

    /** Sequential output file opened with {@code OPEN OUTPUT} (truncated on open). */
    public static final class Writer implements AutoCloseable {
        private final OutputStream out;
        private final Path path;

        public Writer(Path path) {
            this.path = path;
            try {
                this.out = Files.newOutputStream(path);
            } catch (IOException e) {
                throw new UncheckedIOException("cannot open " + path, e);
            }
        }

        public void write(byte[] record) {
            try {
                out.write(record);
            } catch (IOException e) {
                throw new UncheckedIOException("cannot write " + path, e);
            }
        }

        @Override
        public void close() {
            try {
                out.close();
            } catch (IOException e) {
                throw new UncheckedIOException("cannot close " + path, e);
            }
        }
    }
}
