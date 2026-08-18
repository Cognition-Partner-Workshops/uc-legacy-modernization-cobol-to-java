package com.carddemo.statement;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Fixed-width record I/O. The COBOL reference files are undelimited fixed-block records
 * (80 bytes for STMTFILE, 100 for HTMLFILE); the Java port writes the same records one
 * per line so the files stay readable. Both are normalised to a list of records here so
 * parity is judged on record content rather than on line-terminator style.
 */
public final class RecordFiles {

    private RecordFiles() {
    }

    /** Splits a file into fixed-width records, accepting either delimited or undelimited input. */
    public static List<String> readRecords(Path file, int recordLength) {
        String content = read(file);
        List<String> records = new ArrayList<>();
        if (content.indexOf('\n') >= 0) {
            for (String line : content.split("\n", -1)) {
                if (line.isEmpty()) {
                    continue;
                }
                records.add(CobolText.alphanumeric(stripCarriageReturn(line), recordLength));
            }
            return records;
        }
        for (int i = 0; i < content.length(); i += recordLength) {
            records.add(CobolText.alphanumeric(
                    content.substring(i, Math.min(i + recordLength, content.length())), recordLength));
        }
        return records;
    }

    /** Writes one fixed-width record per line. */
    public static void writeRecords(Path file, List<String> records, int recordLength) {
        StringBuilder out = new StringBuilder(records.size() * (recordLength + 1));
        for (String record : records) {
            out.append(CobolText.alphanumeric(record, recordLength)).append('\n');
        }
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, out.toString(), StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }

    private static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    private static String stripCarriageReturn(String line) {
        return line.endsWith("\r") ? line.substring(0, line.length() - 1) : line;
    }
}
