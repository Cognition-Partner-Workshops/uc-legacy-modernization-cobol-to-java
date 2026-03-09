package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * Reads account records from a fixed-length ASCII flat file.
 * Mirrors the COBOL sequential READ of the indexed VSAM ACCTFILE.
 *
 * <p>Each non-blank line is parsed into an {@link AccountRecord} using the
 * field layout defined in copybook CVACT01Y.
 *
 * <p>Implements {@link AutoCloseable} so it can be used in try-with-resources.
 */
public final class AccountFileReader implements AutoCloseable, Iterable<AccountRecord> {

    private final BufferedReader reader;

    public AccountFileReader(Path inputFile) throws IOException {
        this.reader = Files.newBufferedReader(inputFile);
    }

    /**
     * Read the next account record, or {@code null} if end-of-file.
     * Mirrors COBOL paragraph 1000-ACCTFILE-GET-NEXT.
     */
    public AccountRecord readNext() throws IOException {
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                return null; // EOF
            }
            if (line.isBlank()) {
                continue; // skip blank lines
            }
            return AccountRecord.parse(line);
        }
    }

    /**
     * Stream all records from the file.
     */
    public Stream<AccountRecord> stream() throws IOException {
        return reader.lines()
                .filter(line -> !line.isBlank())
                .map(AccountRecord::parse);
    }

    @Override
    public Iterator<AccountRecord> iterator() {
        return new Iterator<>() {
            private AccountRecord next;
            private boolean fetched;

            @Override
            public boolean hasNext() {
                if (!fetched) {
                    try {
                        next = readNext();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    fetched = true;
                }
                return next != null;
            }

            @Override
            public AccountRecord next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                fetched = false;
                return next;
            }
        };
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
