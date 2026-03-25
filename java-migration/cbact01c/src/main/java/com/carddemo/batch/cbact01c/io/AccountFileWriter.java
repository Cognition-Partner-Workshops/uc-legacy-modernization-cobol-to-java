package com.carddemo.batch.cbact01c.io;

import com.carddemo.batch.cbact01c.model.ArrayRecord;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbrRecord1;
import com.carddemo.batch.cbact01c.model.VbrRecord2;
import com.carddemo.batch.cbact01c.util.CobolDecimalParser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the three output files produced by CBACT01C.
 * <p>
 * 1. OUT-FILE:  Fixed-format account records
 * 2. ARRY-FILE: Array-format records with 5 balance entries
 * 3. VBRC-FILE: Variable-length records (alternating VB1 and VB2)
 */
public class AccountFileWriter implements AutoCloseable {

    private final BufferedWriter outWriter;
    private final BufferedWriter arrayWriter;
    private final BufferedWriter vbrWriter;

    public AccountFileWriter(Path outFile, Path arrayFile, Path vbrFile) throws IOException {
        this.outWriter = Files.newBufferedWriter(outFile);
        this.arrayWriter = Files.newBufferedWriter(arrayFile);
        this.vbrWriter = Files.newBufferedWriter(vbrFile);
    }

    /**
     * Writes a fixed-format account record (mirrors COBOL WRITE OUT-ACCT-REC).
     */
    public void writeOutRecord(OutAccountRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(padRight(rec.activeStatus(), 1));
        sb.append(CobolDecimalParser.format(rec.currBal(), 12, 2));
        sb.append(CobolDecimalParser.format(rec.creditLimit(), 12, 2));
        sb.append(CobolDecimalParser.format(rec.cashCreditLimit(), 12, 2));
        sb.append(padRight(rec.openDate(), 10));
        sb.append(padRight(rec.expirationDate(), 10));
        sb.append(padRight(rec.reissueDate(), 10));
        sb.append(CobolDecimalParser.format(rec.currCycCredit(), 12, 2));
        // COMP-3 output -- we write as zoned decimal in the text migration
        sb.append(CobolDecimalParser.format(rec.currCycDebit(), 12, 2));
        sb.append(padRight(rec.groupId(), 10));
        outWriter.write(sb.toString());
        outWriter.newLine();
    }

    /**
     * Writes an array-format record (mirrors COBOL WRITE ARR-ARRAY-REC).
     */
    public void writeArrayRecord(ArrayRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        for (int i = 0; i < ArrayRecord.OCCURS_COUNT; i++) {
            ArrayRecord.BalanceEntry entry = rec.balanceEntries()[i];
            sb.append(CobolDecimalParser.format(entry.currBal(), 12, 2));
            // COMP-3 field -- written as zoned decimal in text migration
            sb.append(CobolDecimalParser.format(entry.currCycDebit(), 12, 2));
        }
        sb.append("    "); // ARR-FILLER PIC X(04)
        arrayWriter.write(sb.toString());
        arrayWriter.newLine();
    }

    /**
     * Writes a VB1 record (variable-length type 1, 12 bytes).
     * Mirrors COBOL: MOVE 12 TO WS-RECD-LEN; WRITE VBR-REC.
     */
    public void writeVbrRecord1(VbrRecord1 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(padRight(rec.activeStatus(), 1));
        vbrWriter.write(sb.toString());
        vbrWriter.newLine();
    }

    /**
     * Writes a VB2 record (variable-length type 2, 39 bytes).
     * Mirrors COBOL: MOVE 39 TO WS-RECD-LEN; WRITE VBR-REC.
     */
    public void writeVbrRecord2(VbrRecord2 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        sb.append(CobolDecimalParser.format(rec.currBal(), 12, 2));
        sb.append(CobolDecimalParser.format(rec.creditLimit(), 12, 2));
        sb.append(padRight(rec.reissueYear(), 4));
        vbrWriter.write(sb.toString());
        vbrWriter.newLine();
    }

    @Override
    public void close() throws IOException {
        outWriter.close();
        arrayWriter.close();
        vbrWriter.close();
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
}
