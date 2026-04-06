package com.carddemo.batch.io;

import com.carddemo.batch.model.OutputAccountRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes {@link OutputAccountRecord} entries to the sequential output file (OUTFILE).
 * Each record is written as a pipe-delimited text line.
 */
public final class OutputFileWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public OutputFileWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    public void write(OutputAccountRecord record) throws IOException {
        writer.write(record.toDelimitedLine());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
