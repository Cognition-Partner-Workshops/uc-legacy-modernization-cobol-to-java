package com.carddemo.batch.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple line-oriented writer that mirrors the COBOL WRITE verb for
 * sequential output files (OUTFILE, ARRYFILE, VBRCFILE).
 *
 * <p>Each record is written as a single line terminated by the platform
 * line separator, replacing the fixed-length or variable-length COBOL
 * record formats with a human-readable, pipe-delimited representation.
 */
public final class RecordWriter implements AutoCloseable {

    private final BufferedWriter writer;
    private final Path path;
    private int recordsWritten;

    public RecordWriter(Path outputFile) throws IOException {
        this.path = outputFile;
        this.writer = Files.newBufferedWriter(outputFile);
        this.recordsWritten = 0;
    }

    /**
     * Write a single delimited record line.
     *
     * @param line the formatted record string (from a record's
     *             {@code toDelimitedString()} method)
     * @throws IOException if the write fails
     */
    public void write(String line) throws IOException {
        writer.write(line);
        writer.newLine();
        recordsWritten++;
    }

    /** Number of records written so far. */
    public int getRecordsWritten() {
        return recordsWritten;
    }

    /** The path this writer is writing to. */
    public Path getPath() {
        return path;
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
