package com.carddemo.batch.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility for reading and writing flat files in batch jobs.
 * Replaces COBOL VSAM file I/O with Java NIO file operations.
 */
public final class BatchFileHelper {

    private BatchFileHelper() {
    }

    /**
     * Read all lines from a file.
     *
     * @param filePath path to the input file
     * @return list of lines
     * @throws IOException if the file cannot be read
     */
    public static List<String> readAllLines(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllLines(filePath);
    }

    /**
     * Write lines to a file, creating it if it doesn't exist.
     *
     * @param filePath path to the output file
     * @param lines    lines to write
     * @throws IOException if the file cannot be written
     */
    public static void writeAllLines(Path filePath, List<String> lines) throws IOException {
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, lines);
    }

    /**
     * Safely pad or truncate a string to a fixed width (COBOL PIC X behavior).
     */
    public static String fixedWidth(String value, int width) {
        if (value == null) {
            return " ".repeat(width);
        }
        if (value.length() >= width) {
            return value.substring(0, width);
        }
        return value + " ".repeat(width - value.length());
    }

    /**
     * Parse a numeric field from a fixed-width string, returning 0 on failure.
     */
    public static long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
