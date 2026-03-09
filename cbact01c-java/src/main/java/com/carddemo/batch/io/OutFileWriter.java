package com.carddemo.batch.io;

import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.util.CobolDecimalParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the flat sequential output file (OUTFILE, LRECL=107, RECFM=FB).
 *
 * <p>Corresponds to COBOL paragraphs 1300-POPUL-ACCT-RECORD and 1350-WRITE-ACCT-RECORD.
 */
public class OutFileWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public OutFileWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    /**
     * Writes one output account record as a fixed-length line.
     */
    public void write(OutAccountRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(CobolDecimalParser.formatUnsignedLong(rec.acctId(), 11));
        sb.append(padRight(rec.activeStatus(), 1));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.currBal(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.creditLimit(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.cashCreditLimit(), 12, 2));
        sb.append(padRight(rec.openDate(), 10));
        sb.append(padRight(rec.expirationDate(), 10));
        sb.append(padRight(rec.reissueDate(), 10));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.currCycCredit(), 12, 2));
        // OUT-ACCT-CURR-CYC-DEBIT is COMP-3 in COBOL (7 bytes packed decimal).
        // In the text-based Java version, we write it as a signed decimal string for readability.
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.currCycDebit(), 12, 2));
        sb.append(padRight(rec.groupId(), 10));
        writer.write(sb.toString());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    private static String padRight(String s, int len) {
        if (s == null) {
            return " ".repeat(len);
        }
        if (s.length() >= len) {
            return s.substring(0, len);
        }
        return s + " ".repeat(len - s.length());
    }
}
