package com.carddemo.batch.io;

import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.util.CobolDecimalParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the array-based sequential output file (ARRYFILE, LRECL=110, RECFM=FB).
 *
 * <p>Corresponds to COBOL paragraphs 1400-POPUL-ARRAY-RECORD and 1450-WRITE-ARRY-RECORD.
 */
public class ArrayFileWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public ArrayFileWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    /**
     * Writes one array record as a fixed-length line.
     */
    public void write(ArrayRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(CobolDecimalParser.formatUnsignedLong(rec.acctId(), 11));

        for (int i = 0; i < ArrayRecord.OCCURS_COUNT; i++) {
            ArrayRecord.ArrayEntry entry = rec.entries()[i];
            sb.append(CobolDecimalParser.formatSignedDecimal(entry.currBal(), 12, 2));
            // ARR-ACCT-CURR-CYC-DEBIT is COMP-3 in COBOL (7 bytes packed).
            // In text mode we write as signed decimal for readability.
            sb.append(CobolDecimalParser.formatSignedDecimal(entry.currCycDebit(), 12, 2));
        }

        // ARR-FILLER PIC X(04)
        sb.append("    ");

        writer.write(sb.toString());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
