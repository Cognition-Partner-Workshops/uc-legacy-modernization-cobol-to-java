package com.carddemo.io;

import com.carddemo.model.ArrayRecord;
import com.carddemo.model.OutputAccountRecord;
import com.carddemo.model.VariableLengthRecord;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Writes the three output files produced by CBACT01C:
 *   1. OUT-FILE:  Fixed-length output account records
 *   2. ARRY-FILE: Array records with 5 balance/debit pairs
 *   3. VBRC-FILE: Variable-length records (two per account)
 *
 * In the COBOL program, OUT-FILE uses COMP-3 for the cycle debit field
 * and ARRY-FILE uses COMP-3 for each debit in the array. Since COMP-3
 * is a binary encoding, in the Java version we write human-readable
 * delimited text instead, preserving the field values and semantics.
 *
 * This is a deliberate modernization choice: replace binary fixed-width
 * output with structured text that can be validated and compared.
 */
public class OutputFileWriter implements AutoCloseable {

    private static final String FIELD_SEPARATOR = "|";

    private final BufferedWriter outWriter;
    private final BufferedWriter arrayWriter;
    private final BufferedWriter vbrcWriter;

    public OutputFileWriter(Path outFile, Path arrayFile, Path vbrcFile) throws IOException {
        this.outWriter = Files.newBufferedWriter(outFile);
        this.arrayWriter = Files.newBufferedWriter(arrayFile);
        this.vbrcWriter = Files.newBufferedWriter(vbrcFile);
    }

    /**
     * Write an output account record (mirrors 1350-WRITE-ACCT-RECORD).
     */
    public void writeOutputRecord(OutputAccountRecord rec) throws IOException {
        outWriter.write(String.join(FIELD_SEPARATOR,
                String.format("%011d", rec.acctId()),
                rec.activeStatus(),
                formatDecimal(rec.currentBalance()),
                formatDecimal(rec.creditLimit()),
                formatDecimal(rec.cashCreditLimit()),
                rec.openDate(),
                rec.expirationDate(),
                rec.reissueDate(),
                formatDecimal(rec.currentCycleCredit()),
                formatDecimal(rec.currentCycleDebit()),
                rec.groupId()
        ));
        outWriter.newLine();
    }

    /**
     * Write an array record (mirrors 1450-WRITE-ARRY-RECORD).
     */
    public void writeArrayRecord(ArrayRecord rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        for (ArrayRecord.BalanceEntry entry : rec.entries()) {
            sb.append(FIELD_SEPARATOR);
            sb.append(formatDecimal(entry.balance()));
            sb.append(FIELD_SEPARATOR);
            sb.append(formatDecimal(entry.debit()));
        }
        arrayWriter.write(sb.toString());
        arrayWriter.newLine();
    }

    /**
     * Write a variable-length record (mirrors 1550/1575-WRITE-VBx-RECORD).
     */
    public void writeVariableLengthRecord(VariableLengthRecord rec) throws IOException {
        if (rec instanceof VariableLengthRecord.Type1 vb1) {
            vbrcWriter.write(String.join(FIELD_SEPARATOR,
                    "VB1",
                    String.format("%011d", vb1.acctId()),
                    vb1.activeStatus()
            ));
        } else if (rec instanceof VariableLengthRecord.Type2 vb2) {
            vbrcWriter.write(String.join(FIELD_SEPARATOR,
                    "VB2",
                    String.format("%011d", vb2.acctId()),
                    formatDecimal(vb2.currentBalance()),
                    formatDecimal(vb2.creditLimit()),
                    vb2.reissueYear()
            ));
        }
        vbrcWriter.newLine();
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format(Locale.US, "%.2f", value);
    }

    @Override
    public void close() throws IOException {
        outWriter.close();
        arrayWriter.close();
        vbrcWriter.close();
    }
}
