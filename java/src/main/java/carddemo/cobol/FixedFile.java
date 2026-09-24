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

    public static List<byte[]> readAll(Path path, int recordLength) {
        byte[] data;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + path, e);
        }
        if (data.length % recordLength != 0) {
            throw new IllegalArgumentException(
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

    /**
     * Variable-length record output in GnuCOBOL {@code COB_VARSEQ_FORMAT=0}: each record is
     * prefixed by a 2-byte big-endian length followed by two NUL bytes.
     */
    public static final class VariableWriter implements AutoCloseable {
        private final Writer writer;

        public VariableWriter(Path path) {
            this.writer = new Writer(path);
        }

        public void write(byte[] record) {
            byte[] header = {(byte) (record.length >> 8), (byte) record.length, 0, 0};
            writer.write(header);
            writer.write(record);
        }

        @Override
        public void close() {
            writer.close();
        }
    }
}
