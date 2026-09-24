package carddemo.cobol;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * In-memory stand-in for a VSAM KSDS ({@code ORGANIZATION IS INDEXED}). Records are kept in
 * primary-key order and can be loaded from / saved to a fixed-record sequential file, which is
 * how the harness materialises indexed datasets.
 */
public final class KeyedFile {
    /** Comparator over the primary-key bytes of a full record. */
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

    public static KeyedFile load(Path path, int recordLength, int keyOffset, int keyLength) {
        KeyedFile file = new KeyedFile(recordLength, keyOffset, keyLength);
        for (byte[] record : FixedFile.readAll(path, recordLength)) {
            file.write(record);
        }
        return file;
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

    /** REWRITE: replaces the record whose key matches; the key must already exist (status 23 otherwise). */
    public void rewrite(byte[] record) {
        checkLength(record);
        byte[] key = keyOf(record);
        if (!records.containsKey(key)) {
            throw new IllegalStateException("rewrite of missing key " + Bytes.ascii(key, 0, key.length));
        }
        records.put(key, record.clone());
    }

    /** Records in ascending primary-key order (sequential READ order). */
    public List<byte[]> records() {
        return records.values().stream().map(byte[]::clone).toList();
    }

    public Collection<byte[]> keys() {
        return records.keySet();
    }

    public int size() {
        return records.size();
    }

    /** Unload to a fixed-record sequential file in primary-key order. */
    public void save(Path path) {
        try (FixedFile.Writer writer = new FixedFile.Writer(path)) {
            for (byte[] record : records.values()) {
                writer.write(record);
            }
        }
    }

    private void checkLength(byte[] record) {
        if (record.length != recordLength) {
            throw new IllegalArgumentException(
                    "record length " + record.length + " does not match " + recordLength);
        }
    }
}
