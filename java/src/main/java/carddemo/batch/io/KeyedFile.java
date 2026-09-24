package carddemo.batch.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Stand-in for a VSAM KSDS: a fixed-record file kept in primary-key order. Records are loaded
 * from a headerless fixed-length file and saved back in key order; random reads, writes and
 * rewrites behave like their COBOL counterparts (status 23 = not found, 22 = duplicate).
 */
public final class KeyedFile {
    private final Path path;
    private final int recordLength;
    private final int keyOffset;
    private final int keyLength;
    private final TreeMap<String, byte[]> records = new TreeMap<>();
    private Iterator<byte[]> cursor;

    private KeyedFile(Path path, int recordLength, int keyOffset, int keyLength) {
        this.path = path;
        this.recordLength = recordLength;
        this.keyOffset = keyOffset;
        this.keyLength = keyLength;
    }

    /** Opens an existing file (INPUT / I-O). */
    public static KeyedFile open(Path path, int recordLength, int keyOffset, int keyLength) throws IOException {
        KeyedFile file = new KeyedFile(path, recordLength, keyOffset, keyLength);
        byte[] all = Files.readAllBytes(path);
        if (all.length % recordLength != 0) {
            throw new IOException(path + ": size " + all.length + " is not a multiple of " + recordLength);
        }
        for (int pos = 0; pos < all.length; pos += recordLength) {
            byte[] rec = Arrays.copyOfRange(all, pos, pos + recordLength);
            file.records.put(file.keyOf(rec), rec);
        }
        file.cursor = file.records.values().iterator();
        return file;
    }

    /** Opens an empty file (OUTPUT); an existing file at the path is replaced on close. */
    public static KeyedFile create(Path path, int recordLength, int keyOffset, int keyLength) {
        KeyedFile file = new KeyedFile(path, recordLength, keyOffset, keyLength);
        file.cursor = file.records.values().iterator();
        return file;
    }

    private String keyOf(byte[] rec) {
        return new String(rec, keyOffset, keyLength, StandardCharsets.ISO_8859_1);
    }

    /** READ NEXT; empty when at end (status 10). */
    public Optional<byte[]> readNext() {
        return cursor.hasNext() ? Optional.of(cursor.next().clone()) : Optional.empty();
    }

    /** READ by primary key; empty means status 23. */
    public Optional<byte[]> read(String key) {
        byte[] rec = records.get(key);
        return rec == null ? Optional.empty() : Optional.of(rec.clone());
    }

    /**
     * READ by an alternate (unique) key: the first record in primary-key order whose bytes at
     * {@code altOffset} match.
     */
    public Optional<byte[]> readByAlternateKey(int altOffset, String altKey) {
        byte[] wanted = altKey.getBytes(StandardCharsets.ISO_8859_1);
        for (byte[] rec : records.values()) {
            if (Arrays.equals(rec, altOffset, altOffset + wanted.length, wanted, 0, wanted.length)) {
                return Optional.of(rec.clone());
            }
        }
        return Optional.empty();
    }

    /** WRITE; false means the key already exists (status 22). */
    public boolean write(byte[] record) {
        byte[] rec = record.clone();
        return records.putIfAbsent(keyOf(rec), rec) == null;
    }

    /** REWRITE; false means no record with that key exists (status 23). */
    public boolean rewrite(byte[] record) {
        byte[] rec = record.clone();
        return records.replace(keyOf(rec), rec) != null;
    }

    public int size() {
        return records.size();
    }

    /** CLOSE: persist the records in primary-key order. */
    public void close() throws IOException {
        byte[] out = new byte[records.size() * recordLength];
        int pos = 0;
        for (Map.Entry<String, byte[]> e : records.entrySet()) {
            System.arraycopy(e.getValue(), 0, out, pos, recordLength);
            pos += recordLength;
        }
        Files.write(path, out);
    }
}
