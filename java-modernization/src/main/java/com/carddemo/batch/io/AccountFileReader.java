package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.util.CobolDataParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads account records from a fixed-width text file that mirrors the
 * COBOL ACCTFILE VSAM KSDS layout (CVACT01Y.cpy, 300 bytes/record).
 * <p>
 * Corresponds to COBOL paragraphs:
 * <ul>
 *   <li>0000-ACCTFILE-OPEN</li>
 *   <li>1000-ACCTFILE-GET-NEXT</li>
 *   <li>9000-ACCTFILE-CLOSE</li>
 * </ul>
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;
    private boolean closed = false;

    public AccountFileReader(Path inputPath) throws IOException {
        this.reader = Files.newBufferedReader(inputPath);
    }

    /**
     * Returns a lazily-populated stream of {@link AccountRecord} instances.
     * The stream should be used within a try-with-resources block.
     */
    public Stream<AccountRecord> stream() {
        Iterator<AccountRecord> iterator = new Iterator<>() {
            private String nextLine = advance();

            private String advance() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank()) {
                            return line;
                        }
                    }
                    return null;
                } catch (IOException e) {
                    throw new UncheckedIOException("Error reading account file", e);
                }
            }

            @Override
            public boolean hasNext() {
                return nextLine != null;
            }

            @Override
            public AccountRecord next() {
                if (nextLine == null) {
                    throw new NoSuchElementException();
                }
                AccountRecord record = CobolDataParser.parseAccountRecord(nextLine);
                nextLine = advance();
                return record;
            }
        };

        Spliterator<AccountRecord> spliterator = Spliterators.spliteratorUnknownSize(
                iterator, Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false);
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            reader.close();
        }
    }
}
