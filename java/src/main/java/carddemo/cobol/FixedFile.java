package carddemo.cobol;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        List<byte[]> records = new ArrayList<>();
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            while (true) {
                byte[] record = in.readNBytes(recordLength);
                if (record.length == 0) {
                    return records;
                }
                if (record.length < recordLength) {
                    throw new IOException(path + ": trailing partial record of " + record.length
                            + " bytes (record length " + recordLength + ")");
                }
                records.add(record);
            }
        }
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
