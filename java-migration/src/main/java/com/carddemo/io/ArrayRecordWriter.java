package com.carddemo.io;

import com.carddemo.model.ArrayRecord;
import com.carddemo.util.CobolNumericParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes array-of-balances records matching the COBOL FD ARRY-FILE layout.
 *
 * Each record has one account ID followed by 5 balance-slot pairs
 * and a 4-byte filler.
 */
public final class ArrayRecordWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public ArrayRecordWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    public void write(ArrayRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.getAcctId()));
        for (int i = 0; i < ArrayRecord.SLOT_COUNT; i++) {
            sb.append(CobolNumericParser.formatSignedDisplay(rec.getAcctCurrBal(i), 10, 2));
            // COMP-3 debit: written as signed display for human-readable output
            sb.append(CobolNumericParser.formatSignedDisplay(rec.getAcctCurrCycDebit(i), 10, 2));
        }
        sb.append("    "); // ARR-FILLER PIC X(04)
        writer.write(sb.toString());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
