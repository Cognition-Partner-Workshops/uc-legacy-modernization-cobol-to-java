package com.carddemo.batch.io;

import com.carddemo.batch.model.ArrayAccountRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes {@link ArrayAccountRecord} entries to the array output file (ARRYFILE).
 * Each record is written as a pipe-delimited text line.
 */
public final class ArrayFileWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public ArrayFileWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    public void write(ArrayAccountRecord record) throws IOException {
        writer.write(record.toDelimitedLine());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
