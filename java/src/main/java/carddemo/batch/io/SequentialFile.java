package carddemo.batch.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Sequential record files. Fixed-length records are stored back to back with no delimiters;
 * variable-length records use the GnuCOBOL RECORD VARYING layout (2-byte big-endian length,
 * 2 zero bytes, then the record).
 */
public final class SequentialFile {
    private SequentialFile() {
    }

    public static Reader openInput(Path path, int recordLength) throws IOException {
        return new Reader(Files.readAllBytes(path), recordLength, path);
    }

    public static Writer openOutput(Path path, boolean variable) throws IOException {
        return new Writer(new BufferedOutputStream(Files.newOutputStream(path)), variable);
    }

    public static final class Reader {
        private final byte[] data;
        private final int recordLength;
        private int pos;

        private Reader(byte[] data, int recordLength, Path source) throws IOException {
            if (data.length % recordLength != 0) {
                throw new IOException(source + ": size " + data.length + " is not a multiple of " + recordLength);
            }
            this.data = data;
            this.recordLength = recordLength;
        }

        /** READ; empty at end of file (status 10). */
        public Optional<byte[]> read() {
            if (pos >= data.length) {
                return Optional.empty();
            }
            byte[] rec = Arrays.copyOfRange(data, pos, pos + recordLength);
            pos += recordLength;
            return Optional.of(rec);
        }
    }

    public static final class Writer {
        private final OutputStream out;
        private final boolean variable;

        private Writer(OutputStream out, boolean variable) {
            this.out = out;
            this.variable = variable;
        }

        public void write(byte[] record) throws IOException {
            if (variable) {
                out.write((record.length >> 8) & 0xFF);
                out.write(record.length & 0xFF);
                out.write(0);
                out.write(0);
            }
            out.write(record);
        }

        public void close() throws IOException {
            out.close();
        }
    }
}
