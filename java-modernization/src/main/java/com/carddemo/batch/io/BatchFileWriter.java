package com.carddemo.batch.io;

import com.carddemo.batch.exception.BatchProcessingException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A simple auto-closeable writer for batch output files.
 * Analogous to the COBOL OPEN OUTPUT / WRITE / CLOSE pattern
 * used for OUTFILE, ARRYFILE, and VBRCFILE.
 */
public final class BatchFileWriter implements AutoCloseable {

    private final BufferedWriter writer;
    private final Path filePath;
    private final String fileDescription;

    /**
     * Opens an output file for writing.
     *
     * @param filePath        the output file path
     * @param fileDescription a human-readable name for error messages
     * @throws BatchProcessingException if the file cannot be opened
     */
    public BatchFileWriter(Path filePath, String fileDescription) {
        this.filePath = filePath;
        this.fileDescription = fileDescription;
        try {
            this.writer = Files.newBufferedWriter(filePath);
        } catch (IOException e) {
            throw new BatchProcessingException(
                    "ERROR OPENING " + fileDescription + ": " + filePath, 999, e);
        }
    }

    /**
     * Writes a single line to the output file, followed by a newline.
     *
     * @param line the line to write
     * @throws BatchProcessingException if the write fails
     */
    public void writeLine(String line) {
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            throw new BatchProcessingException(
                    fileDescription + " WRITE STATUS ERROR: " + filePath, 999, e);
        }
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new BatchProcessingException(
                    "ERROR CLOSING " + fileDescription + ": " + filePath, 999, e);
        }
    }
}
