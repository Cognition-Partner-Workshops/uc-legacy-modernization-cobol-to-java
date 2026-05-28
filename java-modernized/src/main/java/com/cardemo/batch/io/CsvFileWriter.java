package com.cardemo.batch.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generic CSV file writer that replaces the COBOL sequential output file writes.
 */
public final class CsvFileWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public CsvFileWriter(Path filePath, String header) throws IOException {
        this.writer = Files.newBufferedWriter(filePath);
        writer.write(header);
        writer.newLine();
    }

    public void writeLine(String csvLine) throws IOException {
        writer.write(csvLine);
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
