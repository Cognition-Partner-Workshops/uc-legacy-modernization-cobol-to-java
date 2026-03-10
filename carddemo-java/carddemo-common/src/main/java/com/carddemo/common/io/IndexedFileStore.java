package com.carddemo.common.io;

import com.carddemo.common.util.AbendException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Replaces VSAM KSDS indexed random-access reads.
 * Loads an entire indexed file into a {@link Map} keyed by the record key,
 * then provides O(1) lookups by key.
 *
 * @param <K> the key type
 * @param <V> the record type
 */
public class IndexedFileStore<K, V> {

    private final Map<K, V> store;

    /**
     * Load an indexed file into memory.
     *
     * @param filePath     path to the fixed-width data file
     * @param parser       parses each line into a record
     * @param keyExtractor extracts the key from each record
     */
    public IndexedFileStore(Path filePath, RecordParser<V> parser, Function<V, K> keyExtractor) {
        Map<K, V> map = new LinkedHashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                V record = parser.parse(line);
                K key = keyExtractor.apply(record);
                map.put(key, record);
            }
        } catch (IOException e) {
            throw new AbendException(999,
                    "Error loading indexed file: " + filePath + ", Status: " + e.getMessage(), e);
        }
        this.store = Collections.unmodifiableMap(map);
    }

    /**
     * Construct from an existing map (for testing).
     */
    public IndexedFileStore(Map<K, V> map) {
        this.store = Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    /**
     * Random read by key. Equivalent to COBOL READ ... KEY IS ... INVALID KEY.
     *
     * @param key the record key
     * @return the record if found
     */
    public Optional<V> read(K key) {
        return Optional.ofNullable(store.get(key));
    }

    /**
     * Check if a key exists.
     */
    public boolean containsKey(K key) {
        return store.containsKey(key);
    }

    /**
     * Get all records in insertion order.
     */
    public Map<K, V> getAll() {
        return store;
    }

    /**
     * Get the number of records.
     */
    public int size() {
        return store.size();
    }
}
