package com.carddemo.batch.io;

import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;
import com.carddemo.batch.util.CobolDecimalParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the three output files produced by CBACT01C:
 * <ol>
 *   <li>OUTFILE  — flat account record (LRECL 107, FB)</li>
 *   <li>ARRYFILE — array-based balance record (LRECL 110, FB)</li>
 *   <li>VBRCFILE — variable-length records (LRECL 84, VB)</li>
 * </ol>
 *
 * <p>In the Java equivalent, each output line is a text representation
 * of the fixed/variable-length COBOL record. COMP-3 fields are written
 * as their display-equivalent decimal text for readability.
 */
public final class AccountFileWriter implements AutoCloseable {

    private final BufferedWriter outWriter;
    private final BufferedWriter arrWriter;
    private final BufferedWriter vbrWriter;

    public AccountFileWriter(Path outFile, Path arrFile, Path vbrFile) throws IOException {
        this.outWriter = Files.newBufferedWriter(outFile);
        this.arrWriter = Files.newBufferedWriter(arrFile);
        this.vbrWriter = Files.newBufferedWriter(vbrFile);
    }

    /**
     * Writes one flat-output account record (corresponds to 1350-WRITE-ACCT-RECORD).
     */
    public void writeOutRecord(OutAccountRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(padRight(rec.activeStatus(), 1));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.currBal(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.creditLimit(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.cashCreditLimit(), 12, 2));
        sb.append(padRight(rec.openDate(), 10));
        sb.append(padRight(rec.expiraionDate(), 10));
        sb.append(padRight(rec.reissueDate(), 10));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.currCycCredit(), 12, 2));
        // COMP-3 field: write as packed-decimal display equivalent (7 hex-pairs)
        sb.append(formatComp3(rec.currCycDebit(), 12, 2));
        sb.append(padRight(rec.groupId(), 10));
        outWriter.write(sb.toString());
        outWriter.newLine();
    }

    /**
     * Writes one array record (corresponds to 1450-WRITE-ARRY-RECORD).
     */
    public void writeArrayRecord(ArrayRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        for (ArrayRecord.BalanceEntry entry : rec.balanceEntries()) {
            sb.append(CobolDecimalParser.formatSignedDecimal(entry.currBal(), 12, 2));
            sb.append(formatComp3(entry.currCycDebit(), 12, 2));
        }
        sb.append(padRight(rec.filler(), 4));
        arrWriter.write(sb.toString());
        arrWriter.newLine();
    }

    /**
     * Writes one VB1 record (corresponds to 1550-WRITE-VB1-RECORD).
     */
    public void writeVbrcRecord1(VbrcRecord1 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(padRight(rec.activeStatus(), 1));
        vbrWriter.write(sb.toString());
        vbrWriter.newLine();
    }

    /**
     * Writes one VB2 record (corresponds to 1575-WRITE-VB2-RECORD).
     */
    public void writeVbrcRecord2(VbrcRecord2 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.currBal(), 12, 2));
        sb.append(CobolDecimalParser.formatSignedDecimal(rec.creditLimit(), 12, 2));
        sb.append(padRight(rec.reissueYyyy(), 4));
        vbrWriter.write(sb.toString());
        vbrWriter.newLine();
    }

    /**
     * Formats a COMP-3 (packed decimal) field as a display string.
     * In the Java text-mode output, we use a compact signed representation.
     *
     * <p>COMP-3 PIC S9(10)V99 occupies 7 bytes in COBOL. In our text output
     * we represent it as a 7-character hex-like display for traceability,
     * but for practical purposes we output the zoned-decimal equivalent.
     */
    private static String formatComp3(BigDecimal value, int totalDigits, int decimalPlaces) {
        // For the text-mode output, we write COMP-3 as its display equivalent
        // using the same zoned-decimal format for consistency
        return CobolDecimalParser.formatSignedDecimal(value, totalDigits, decimalPlaces);
    }

    private static String padRight(String s, int length) {
        if (s == null) {
            return " ".repeat(length);
        }
        if (s.length() >= length) {
            return s.substring(0, length);
        }
        return s + " ".repeat(length - s.length());
    }

    @Override
    public void close() throws IOException {
        outWriter.close();
        arrWriter.close();
        vbrWriter.close();
    }
}
