package com.carddemo.io;

import com.carddemo.model.OutputAccountRecord;
import com.carddemo.util.CobolNumericParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes flat-file output records matching the COBOL FD OUT-FILE layout.
 */
public final class OutputAccountWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public OutputAccountWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    public void write(OutputAccountRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(rec.acctActiveStatus());
        sb.append(CobolNumericParser.formatSignedDisplay(rec.acctCurrBal(), 10, 2));
        sb.append(CobolNumericParser.formatSignedDisplay(rec.acctCreditLimit(), 10, 2));
        sb.append(CobolNumericParser.formatSignedDisplay(rec.acctCashCreditLimit(), 10, 2));
        sb.append(padRight(rec.acctOpenDate(), 10));
        sb.append(padRight(rec.acctExpiraionDate(), 10));
        sb.append(padRight(rec.acctReissueDate(), 10));
        sb.append(CobolNumericParser.formatSignedDisplay(rec.acctCurrCycCredit(), 10, 2));
        // COMP-3 debit: we write it as signed display for human-readable output
        sb.append(CobolNumericParser.formatSignedDisplay(rec.acctCurrCycDebit(), 10, 2));
        sb.append(padRight(rec.acctGroupId(), 10));
        writer.write(sb.toString());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private static String padRight(String s, int len) {
        if (s == null) return " ".repeat(len);
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }
}
