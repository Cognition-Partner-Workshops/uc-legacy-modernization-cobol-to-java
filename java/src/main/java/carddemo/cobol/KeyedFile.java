package carddemo.cobol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Read-only stand-in for a VSAM KSDS opened {@code INPUT} with {@code ACCESS MODE IS SEQUENTIAL}.
 * The dataset is supplied as its unloaded image (IDCAMS REPRO output: fixed-length records with
 * no delimiters, in any order) and is read back in ascending primary-key order, unsigned byte
 * comparison, which is the order a sequential READ of the cluster returns.
 */
public final class KeyedFile {
    private final List<byte[]> records;
    private int next;

    private KeyedFile(List<byte[]> records) {
        this.records = records;
    }

    /** {@code OPEN INPUT}: status 35 when the dataset does not exist, 30 for unreadable or malformed data. */
    public static KeyedFile openInput(Path path, int recordLength, int keyOffset, int keyLength)
            throws FileStatusException {
        byte[] image;
        try {
            image = Files.readAllBytes(path);
        } catch (NoSuchFileException e) {
            throw new FileStatusException("35", path + " not found", e);
        } catch (IOException e) {
            throw new FileStatusException("30", "cannot read " + path, e);
        }
        if (image.length % recordLength != 0) {
            throw new FileStatusException("30",
                    path + ": size " + image.length + " is not a multiple of " + recordLength, null);
        }
        TreeMap<byte[], byte[]> byKey = new TreeMap<>(Arrays::compareUnsigned);
        for (int offset = 0; offset < image.length; offset += recordLength) {
            byte[] record = Bytes.slice(image, offset, recordLength);
            byte[] key = Bytes.slice(record, keyOffset, keyLength);
            if (byKey.put(key, record) != null) {
                throw new FileStatusException("30",
                        path + ": duplicate key " + Bytes.ascii(key, 0, keyLength), null);
            }
        }
        return new KeyedFile(new ArrayList<>(byKey.values()));
    }

    /** Sequential READ: the next record, or {@code null} at end of file (status 10). */
    public byte[] readNext() {
        return next < records.size() ? records.get(next++).clone() : null;
    }

    public Iterator<byte[]> remaining() {
        return records.subList(next, records.size()).iterator();
    }
}
