package carddemo.cobol;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * In-memory stand-in for a VSAM KSDS ({@code ORGANIZATION IS INDEXED}). Records are kept in
 * primary-key order (unsigned byte order, as VSAM collates) and are materialised from a
 * fixed-record sequential file.
 */
public final class KeyedFile {
    private static final Comparator<byte[]> KEY_ORDER = Arrays::compareUnsigned;

    private final int recordLength;
    private final int keyOffset;
    private final int keyLength;
    private final TreeMap<byte[], byte[]> records = new TreeMap<>(KEY_ORDER);

    public KeyedFile(int recordLength, int keyOffset, int keyLength) {
        this.recordLength = recordLength;
        this.keyOffset = keyOffset;
        this.keyLength = keyLength;
    }

    /**
     * {@code OPEN INPUT} of an indexed file. Returns the COBOL file status instead of the file:
     * {@code 00} success, {@code 35} dataset not found, {@code 30} any other permanent I/O error
     * (unreadable file, size not a multiple of the record length).
     */
    public static Opened open(Path path, int recordLength, int keyOffset, int keyLength) {
        KeyedFile file = new KeyedFile(recordLength, keyOffset, keyLength);
        try {
            for (byte[] record : FixedFile.readAll(path, recordLength)) {
                file.write(record);
            }
        } catch (NoSuchFileException e) {
            return new Opened("35", null);
        } catch (IOException | IllegalStateException e) {
            return new Opened("30", null);
        }
        return new Opened("00", file);
    }

    /** Result of an OPEN: the file status and, when it is {@code 00}, the opened file. */
    public record Opened(String status, KeyedFile file) {
        public boolean ok() {
            return "00".equals(status);
        }
    }

    public int recordLength() {
        return recordLength;
    }

    public byte[] keyOf(byte[] record) {
        return Bytes.slice(record, keyOffset, keyLength);
    }

    /** READ by primary key; empty when the key is absent (status 23). */
    public Optional<byte[]> read(byte[] key) {
        byte[] found = records.get(key);
        return Optional.ofNullable(found).map(byte[]::clone);
    }

    public boolean contains(byte[] key) {
        return records.containsKey(key);
    }

    /** WRITE: inserts a record; a duplicate primary key is a status-22 error. */
    public void write(byte[] record) {
        checkLength(record);
        byte[] key = keyOf(record);
        if (records.containsKey(key)) {
            throw new IllegalStateException("duplicate key " + Bytes.ascii(key, 0, key.length));
        }
        records.put(key, record.clone());
    }

    /** Records in ascending primary-key order (sequential READ order). */
    public List<byte[]> records() {
        return records.values().stream().map(byte[]::clone).toList();
    }

    /** Sequential-access cursor: successive {@code READ ... NEXT} calls from the start of the file. */
    public Cursor cursor() {
        return new Cursor(records.values().iterator());
    }

    /** A sequential read position; {@link #next()} is empty at end of file (status 10). */
    public static final class Cursor {
        private final Iterator<byte[]> iterator;

        private Cursor(Iterator<byte[]> iterator) {
            this.iterator = iterator;
        }

        public Optional<byte[]> next() {
            return iterator.hasNext() ? Optional.of(iterator.next().clone()) : Optional.empty();
        }
    }

    public int size() {
        return records.size();
    }

    private void checkLength(byte[] record) {
        if (record.length != recordLength) {
            throw new IllegalArgumentException(
                    "record length " + record.length + " does not match " + recordLength);
        }
    }
}
