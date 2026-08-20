package com.carddemo.batch.ebcdic;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

/**
 * CSUSR01Y is display-character data only; no packed-decimal decoding is used here.
 */
public class FixedLengthEbcdicReader implements ItemStreamReader<UsrsecRecord> {
    private static final int RECORD_LENGTH = 80;
    private static final Charset IBM037 = Charset.forName("IBM037");

    private final Path inputPath;
    private InputStream inputStream;
    private int recordNumber;

    public FixedLengthEbcdicReader(Path inputPath) {
        this.inputPath = inputPath;
    }

    @Override
    public void open(ExecutionContext executionContext) {
        try {
            inputStream = Files.newInputStream(inputPath);
            recordNumber = executionContext.getInt("usrsec.recordNumber", 0);
            long skipped = inputStream.skip((long) recordNumber * RECORD_LENGTH);
            if (skipped != (long) recordNumber * RECORD_LENGTH) {
                throw new IOException("Unable to resume USRSEC reader at record " + recordNumber);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to open USRSEC input " + inputPath, exception);
        }
    }

    @Override
    public UsrsecRecord read() throws Exception {
        byte[] record = inputStream.readNBytes(RECORD_LENGTH);
        if (record.length == 0) {
            return null;
        }
        if (record.length != RECORD_LENGTH) {
            throw new IOException("USRSEC input ended with a partial 80-byte record");
        }
        String decoded = new String(record, IBM037);
        recordNumber++;
        return new UsrsecRecord(
                trim(decoded, 0, 8),
                trim(decoded, 8, 28),
                trim(decoded, 28, 48),
                trim(decoded, 48, 56),
                trim(decoded, 56, 57));
    }

    private String trim(String value, int start, int end) {
        return value.substring(start, end).stripTrailing();
    }

    @Override
    public void update(ExecutionContext executionContext) {
        executionContext.putInt("usrsec.recordNumber", recordNumber);
    }

    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException exception) {
                throw new UncheckedIOException("Unable to close USRSEC input " + inputPath, exception);
            }
        }
    }
}
