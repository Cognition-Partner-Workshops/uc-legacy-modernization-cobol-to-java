package com.carddemo.common.io;

import com.carddemo.common.util.AbendException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes typed records to a sequential fixed-width file.
 * Replaces COBOL WRITE ... FROM pattern.
 *
 * @param <T> the record type
 */
public class SequentialFileWriter<T> implements AutoCloseable {

    private final BufferedWriter writer;
    private final RecordFormatter<T> formatter;
    private final Path filePath;

    public SequentialFileWriter(Path filePath, RecordFormatter<T> formatter) {
        this.filePath = filePath;
        this.formatter = formatter;
        try {
            this.writer = Files.newBufferedWriter(filePath);
        } catch (IOException e) {
            throw new AbendException(999,
                    "Error opening output file: " + filePath + ", Status: " + e.getMessage(), e);
        }
    }

    /**
     * Write a single record as a fixed-width line.
     */
    public void write(T record) {
        try {
            writer.write(formatter.format(record));
            writer.newLine();
        } catch (IOException e) {
            throw new AbendException(999,
                    "Error writing to file: " + filePath + ", Status: " + e.getMessage(), e);
        }
    }

    /**
     * Write a raw string line (for variable-length or special records).
     */
    public void writeLine(String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new AbendException(999,
                    "Error writing to file: " + filePath + ", Status: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new AbendException(999,
                    "Error closing output file: " + filePath + ", Status: " + e.getMessage(), e);
        }
    }
}
