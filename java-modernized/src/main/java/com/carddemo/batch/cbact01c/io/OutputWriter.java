package com.carddemo.batch.cbact01c.io;

import com.carddemo.batch.cbact01c.model.ArrayAccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayAccountRecord.BalanceDebitPair;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbrRecord1;
import com.carddemo.batch.cbact01c.model.VbrRecord2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes the three output files produced by the CBACT01C batch program.
 *
 * <p>In the modernized Java version, output is written as CSV for
 * interoperability, while preserving the same data and transformations
 * as the original COBOL program.
 */
public final class OutputWriter {

    /** CSV header for the OUT account file. */
    static final String OUT_HEADER =
            "ACCT_ID,ACTIVE_STATUS,CURR_BAL,CREDIT_LIMIT,CASH_CREDIT_LIMIT,"
                    + "OPEN_DATE,EXPIRATION_DATE,REISSUE_DATE,CURR_CYC_CREDIT,"
                    + "CURR_CYC_DEBIT,GROUP_ID";

    /** CSV header for the ARRAY file. */
    static final String ARR_HEADER;

    static {
        StringBuilder sb = new StringBuilder("ACCT_ID");
        for (int i = 1; i <= ArrayAccountRecord.OCCURS_COUNT; i++) {
            sb.append(",BAL_").append(i).append(",DEBIT_").append(i);
        }
        ARR_HEADER = sb.toString();
    }

    /** CSV header for the VBR file (both record types interleaved). */
    static final String VBR_HEADER =
            "RECORD_TYPE,ACCT_ID,FIELD1,FIELD2,FIELD3";

    private OutputWriter() {
        // utility class
    }

    /**
     * Writes a list of {@link OutAccountRecord}s to the given path as CSV.
     *
     * @param records the records to write
     * @param path    output file path
     * @throws IOException if writing fails
     */
    public static void writeOutFile(List<OutAccountRecord> records, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(OUT_HEADER);
            writer.newLine();
            for (OutAccountRecord r : records) {
                writer.write(formatOutRecord(r));
                writer.newLine();
            }
        }
    }

    /**
     * Writes a list of {@link ArrayAccountRecord}s to the given path as CSV.
     *
     * @param records the records to write
     * @param path    output file path
     * @throws IOException if writing fails
     */
    public static void writeArrayFile(List<ArrayAccountRecord> records, Path path)
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(ARR_HEADER);
            writer.newLine();
            for (ArrayAccountRecord r : records) {
                writer.write(formatArrayRecord(r));
                writer.newLine();
            }
        }
    }

    /**
     * Writes interleaved VBR record 1 and record 2 pairs to the given path as CSV.
     * For each account, a VB1 line is followed by a VB2 line, matching the
     * COBOL write order (1550 then 1575).
     *
     * @param vb1Records list of VBR record 1 entries
     * @param vb2Records list of VBR record 2 entries (same size as vb1Records)
     * @param path       output file path
     * @throws IOException if writing fails
     */
    public static void writeVbrFile(List<VbrRecord1> vb1Records,
                                    List<VbrRecord2> vb2Records,
                                    Path path) throws IOException {
        if (vb1Records.size() != vb2Records.size()) {
            throw new IllegalArgumentException(
                    "VBR record lists must have the same size");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(VBR_HEADER);
            writer.newLine();
            for (int i = 0; i < vb1Records.size(); i++) {
                writer.write(formatVbrRecord1(vb1Records.get(i)));
                writer.newLine();
                writer.write(formatVbrRecord2(vb2Records.get(i)));
                writer.newLine();
            }
        }
    }

    // ----------------------------------------------------------------
    // Formatting helpers
    // ----------------------------------------------------------------

    static String formatOutRecord(OutAccountRecord r) {
        return String.join(",",
                String.format("%011d", r.acctId()),
                r.activeStatus(),
                formatDecimal(r.currBal()),
                formatDecimal(r.creditLimit()),
                formatDecimal(r.cashCreditLimit()),
                r.openDate(),
                r.expirationDate(),
                r.reissueDate().trim(),
                formatDecimal(r.currCycCredit()),
                formatDecimal(r.currCycDebit()),
                r.groupId()
        );
    }

    static String formatArrayRecord(ArrayAccountRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", r.acctId()));
        for (BalanceDebitPair pair : r.balanceDebitPairs()) {
            sb.append(',').append(formatDecimal(pair.currBal()));
            sb.append(',').append(formatDecimal(pair.currCycDebit()));
        }
        return sb.toString();
    }

    static String formatVbrRecord1(VbrRecord1 r) {
        return String.join(",",
                "VB1",
                String.format("%011d", r.acctId()),
                r.activeStatus(),
                "",
                ""
        );
    }

    static String formatVbrRecord2(VbrRecord2 r) {
        return String.join(",",
                "VB2",
                String.format("%011d", r.acctId()),
                formatDecimal(r.currBal()),
                formatDecimal(r.creditLimit()),
                r.reissueYear()
        );
    }

    private static String formatDecimal(BigDecimal value) {
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
