package com.carddemo.batch.io;

import com.carddemo.batch.exception.BatchProcessingException;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.service.AccountRecordParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads account records from a fixed-width text file, analogous to the
 * COBOL sequential READ of the ACCTFILE VSAM KSDS.
 * <p>
 * Implements {@link AutoCloseable} so it can be used in try-with-resources,
 * mirroring the COBOL OPEN INPUT / CLOSE pattern.
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;
    private final Path filePath;

    /**
     * Opens the account file for reading (analogous to 0000-ACCTFILE-OPEN).
     *
     * @param filePath the path to the account data file
     * @throws BatchProcessingException if the file cannot be opened
     */
    public AccountFileReader(Path filePath) {
        this.filePath = filePath;
        try {
            this.reader = Files.newBufferedReader(filePath);
        } catch (IOException e) {
            throw new BatchProcessingException(
                    "ERROR OPENING ACCTFILE: " + filePath, 999, e);
        }
    }

    /**
     * Returns a sequential stream of {@link AccountRecord} objects,
     * analogous to the COBOL PERFORM UNTIL END-OF-FILE loop with
     * 1000-ACCTFILE-GET-NEXT.
     *
     * @return stream of parsed account records
     */
    public Stream<AccountRecord> stream() {
        Iterator<AccountRecord> iterator = new Iterator<>() {
            private String nextLine = readNextNonEmptyLine();

            @Override
            public boolean hasNext() {
                return nextLine != null;
            }

            @Override
            public AccountRecord next() {
                if (nextLine == null) {
                    throw new NoSuchElementException("End of account file reached");
                }
                String current = nextLine;
                nextLine = readNextNonEmptyLine();
                return AccountRecordParser.parse(current);
            }
        };

        Spliterator<AccountRecord> spliterator = Spliterators.spliteratorUnknownSize(
                iterator, Spliterator.ORDERED | Spliterator.NONNULL);
        return StreamSupport.stream(spliterator, false);
    }

    private String readNextNonEmptyLine() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    return line;
                }
            }
            return null;
        } catch (IOException e) {
            throw new BatchProcessingException(
                    "ERROR READING ACCOUNT FILE: " + filePath, 999, e);
        }
    }

    /**
     * Closes the account file (analogous to 9000-ACCTFILE-CLOSE).
     */
    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new BatchProcessingException(
                    "ERROR CLOSING ACCOUNT FILE: " + filePath, 999, e);
        }
    }
}
