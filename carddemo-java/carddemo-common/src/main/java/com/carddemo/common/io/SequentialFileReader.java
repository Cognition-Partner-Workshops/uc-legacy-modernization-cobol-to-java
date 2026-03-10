package com.carddemo.common.io;

import com.carddemo.common.util.AbendException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Reads a sequential fixed-width file and parses each line into a typed record.
 * Replaces COBOL sequential READ ... INTO pattern.
 *
 * @param <T> the record type
 */
public class SequentialFileReader<T> implements AutoCloseable, Iterable<T> {

    private final BufferedReader reader;
    private final RecordParser<T> parser;
    private final Path filePath;

    public SequentialFileReader(Path filePath, RecordParser<T> parser) {
        this.filePath = filePath;
        this.parser = parser;
        try {
            this.reader = Files.newBufferedReader(filePath);
        } catch (IOException e) {
            throw new AbendException(999,
                    "Error opening file: " + filePath + ", Status: " + e.getMessage(), e);
        }
    }

    /**
     * Read the next record, or null if EOF.
     */
    public T readNext() {
        try {
            String line = reader.readLine();
            if (line == null) {
                return null;
            }
            return parser.parse(line);
        } catch (IOException e) {
            throw new AbendException(999,
                    "Error reading file: " + filePath + ", Status: " + e.getMessage(), e);
        }
    }

    /**
     * Read all records into a list.
     */
    public List<T> readAll() {
        List<T> records = new ArrayList<>();
        T record;
        while ((record = readNext()) != null) {
            records.add(record);
        }
        return records;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private T next = readNext();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public T next() {
                if (next == null) {
                    throw new NoSuchElementException();
                }
                T current = next;
                next = readNext();
                return current;
            }
        };
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new AbendException(999,
                    "Error closing file: " + filePath + ", Status: " + e.getMessage(), e);
        }
    }
}
