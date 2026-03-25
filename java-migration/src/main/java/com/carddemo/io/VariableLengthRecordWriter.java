package com.carddemo.io;

import com.carddemo.model.VariableLengthRecord1;
import com.carddemo.model.VariableLengthRecord2;
import com.carddemo.util.CobolNumericParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes variable-length records matching the COBOL FD VBRC-FILE layout.
 *
 * The original COBOL file uses RECORDING MODE V with records varying
 * from 10 to 80 bytes.  In Java we write each logical record on its own
 * line (newline-delimited), preserving the exact field content and length.
 */
public final class VariableLengthRecordWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public VariableLengthRecordWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    /** Write a type-1 record (12 bytes: account-id + active-status). */
    public void writeType1(VariableLengthRecord1 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(rec.acctActiveStatus());
        writer.write(sb.toString());
        writer.newLine();
    }

    /** Write a type-2 record (39 bytes: account-id + bal + limit + reissue-year). */
    public void writeType2(VariableLengthRecord2 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(CobolNumericParser.formatSignedDisplay(rec.acctCurrBal(), 10, 2));
        sb.append(CobolNumericParser.formatSignedDisplay(rec.acctCreditLimit(), 10, 2));
        sb.append(padRight(rec.acctReissueYear(), 4));
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
