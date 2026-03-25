package com.carddemo.batch.io;

import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutAccountRecord;
import com.carddemo.batch.model.VbrRecord1;
import com.carddemo.batch.model.VbrRecord2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the three output files produced by CBACT01C:
 * <ol>
 *   <li>OUT-FILE: sequential flat file with account summary records</li>
 *   <li>ARRY-FILE: sequential flat file with array-structured records</li>
 *   <li>VBRC-FILE: variable-length record file (two records per account)</li>
 * </ol>
 */
public final class AccountFileWriter implements AutoCloseable {

    private final BufferedWriter outWriter;
    private final BufferedWriter arryWriter;
    private final BufferedWriter vbrcWriter;

    public AccountFileWriter(Path outFile, Path arryFile, Path vbrcFile) throws IOException {
        this.outWriter = Files.newBufferedWriter(outFile);
        this.arryWriter = Files.newBufferedWriter(arryFile);
        this.vbrcWriter = Files.newBufferedWriter(vbrcFile);
    }

    /**
     * Write an output account record (OUT-FILE).
     *
     * The COBOL format includes COMP-3 for CURR-CYC-DEBIT; in the Java
     * version we write a delimited text format since there is no mainframe
     * binary requirement.
     */
    public void writeOutRecord(OutAccountRecord rec) throws IOException {
        outWriter.write(formatOutRecord(rec));
        outWriter.newLine();
    }

    /**
     * Write an array record (ARRY-FILE).
     */
    public void writeArrayRecord(ArrayRecord rec) throws IOException {
        arryWriter.write(formatArrayRecord(rec));
        arryWriter.newLine();
    }

    /**
     * Write variable-length record type 1 (short — 12 bytes in COBOL).
     */
    public void writeVbrRecord1(VbrRecord1 rec) throws IOException {
        vbrcWriter.write(formatVbrRecord1(rec));
        vbrcWriter.newLine();
    }

    /**
     * Write variable-length record type 2 (long — 39 bytes in COBOL).
     */
    public void writeVbrRecord2(VbrRecord2 rec) throws IOException {
        vbrcWriter.write(formatVbrRecord2(rec));
        vbrcWriter.newLine();
    }

    // ---- Formatting methods (pipe-delimited for modern consumption) ----

    static String formatOutRecord(OutAccountRecord rec) {
        return String.join("|",
                String.format("%011d", rec.acctId()),
                rec.activeStatus(),
                formatDecimal(rec.currentBalance()),
                formatDecimal(rec.creditLimit()),
                formatDecimal(rec.cashCreditLimit()),
                rec.openDate() != null ? rec.openDate() : "",
                rec.expirationDate() != null ? rec.expirationDate() : "",
                rec.reissueDate() != null ? rec.reissueDate() : "",
                formatDecimal(rec.currentCycleCredit()),
                formatDecimal(rec.currentCycleDebit()),
                rec.groupId() != null ? rec.groupId() : ""
        );
    }

    static String formatArrayRecord(ArrayRecord rec) {
        var sb = new StringBuilder();
        sb.append(String.format("%011d", rec.acctId()));
        for (var entry : rec.balanceEntries()) {
            sb.append('|');
            sb.append(formatDecimal(entry.currentBalance()));
            sb.append('|');
            sb.append(formatDecimal(entry.currentCycleDebit()));
        }
        return sb.toString();
    }

    static String formatVbrRecord1(VbrRecord1 rec) {
        return String.format("%011d", rec.acctId()) + rec.activeStatus();
    }

    static String formatVbrRecord2(VbrRecord2 rec) {
        return String.join("|",
                String.format("%011d", rec.acctId()),
                formatDecimal(rec.currentBalance()),
                formatDecimal(rec.creditLimit()),
                rec.reissueYear() != null ? rec.reissueYear() : ""
        );
    }

    private static String formatDecimal(BigDecimal value) {
        return value != null ? value.toPlainString() : "0.00";
    }

    @Override
    public void close() throws IOException {
        outWriter.close();
        arryWriter.close();
        vbrcWriter.close();
    }
}
