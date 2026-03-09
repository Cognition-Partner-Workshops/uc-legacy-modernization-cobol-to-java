package com.carddemo.batch.io;

import com.carddemo.batch.model.VbRecord1;
import com.carddemo.batch.model.VbRecord2;
import com.carddemo.batch.util.CobolDecimalParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the variable-length record file (VBRCFILE, LRECL=84, RECFM=VB).
 *
 * <p>In COBOL, VBRC-FILE uses RECORDING MODE IS V, meaning each physical
 * record carries a 4-byte Record Descriptor Word (RDW) prefix. In the
 * modernized text-based version, each record is a variable-length text
 * line (no RDW), preserving the logical content.
 *
 * <p>Corresponds to COBOL paragraphs:
 * <ul>
 *   <li>1500-POPUL-VBRC-RECORD</li>
 *   <li>1550-WRITE-VB1-RECORD (WS-RECD-LEN = 12)</li>
 *   <li>1575-WRITE-VB2-RECORD (WS-RECD-LEN = 39)</li>
 * </ul>
 */
public class VbRecordFileWriter implements AutoCloseable {

    private final BufferedWriter writer;

    public VbRecordFileWriter(Path path) throws IOException {
        this.writer = Files.newBufferedWriter(path);
    }

    /**
     * Writes a VB1 record (12 bytes logical content).
     */
    public void writeVb1(VbRecord1 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(CobolDecimalParser.formatUnsignedLong(rec.acctId(), 11));
        sb.append(padRight(rec.activeStatus(), 1));
        writer.write(sb.toString());
        writer.newLine();
    }

    /**
     * Writes a VB2 record (39 bytes logical content).
     */
    public void writeVb2(VbRecord2 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(CobolDecimalParser.formatUnsignedLong(rec.acctId(), 11));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.currBal(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.creditLimit(), 12, 2));
        sb.append(padRight(rec.reissueYear(), 4));
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
