package com.cardemo.batch;

import com.cardemo.batch.io.CobolDataParser;
import com.cardemo.batch.model.*;
import com.cardemo.batch.util.DateConverter;
import com.cardemo.batch.util.DateConverter.InputFormat;
import com.cardemo.batch.util.DateConverter.OutputFormat;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ equivalent of CBACT01C.CBL — reads an indexed account file
 * sequentially and writes three output files:
 * <ol>
 *   <li>OUT-FILE  — selected account fields (fixed-width)</li>
 *   <li>ARRY-FILE — array-structured balance records</li>
 *   <li>VBRC-FILE — variable-length status and balance records</li>
 * </ol>
 */
public final class AccountBatchProcessor {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_3   = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arrayFile;
    private final Path vbrFile;

    private final List<String> displayLog = new ArrayList<>();

    public AccountBatchProcessor(Path inputFile, Path outFile,
                                 Path arrayFile, Path vbrFile) {
        this.inputFile = inputFile;
        this.outFile   = outFile;
        this.arrayFile = arrayFile;
        this.vbrFile   = vbrFile;
    }

    /**
     * Execute the batch job. Returns the number of records processed.
     */
    public int execute() throws IOException {
        display("START OF EXECUTION OF PROGRAM CBACT01C");

        int count = 0;

        try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
             BufferedWriter outWriter   = Files.newBufferedWriter(outFile, StandardCharsets.UTF_8);
             BufferedWriter arrWriter   = Files.newBufferedWriter(arrayFile, StandardCharsets.UTF_8);
             BufferedWriter vbrWriter   = Files.newBufferedWriter(vbrFile, StandardCharsets.UTF_8)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                AccountRecord acct = CobolDataParser.parseAccountRecord(line);
                display(formatAccountDisplay(acct));

                OutAccountRecord outRec = buildOutRecord(acct);
                outWriter.write(formatOutRecord(outRec));
                outWriter.newLine();

                ArrayAccountRecord arrRec = buildArrayRecord(acct);
                arrWriter.write(formatArrayRecord(arrRec));
                arrWriter.newLine();

                VarLengthRecord.StatusRecord vb1 = buildStatusRecord(acct);
                VarLengthRecord.BalanceRecord vb2 = buildBalanceRecord(acct);
                display("VBRC-REC1:" + formatStatusRecord(vb1));
                display("VBRC-REC2:" + formatBalanceRecord(vb2));
                vbrWriter.write(formatStatusRecord(vb1));
                vbrWriter.newLine();
                vbrWriter.write(formatBalanceRecord(vb2));
                vbrWriter.newLine();

                count++;
            }
        }

        display("END OF EXECUTION OF PROGRAM CBACT01C");
        return count;
    }

    // ---- record builders (mirrors COBOL PERFORM paragraphs) ----

    /**
     * 1300-POPUL-ACCT-RECORD — populate the OUT-FILE record.
     * Applies date conversion and the zero-debit defaulting rule.
     */
    OutAccountRecord buildOutRecord(AccountRecord acct) {
        String convertedReissueDate = DateConverter.convert(
                acct.reissueDate(), InputFormat.YYYY_MM_DD, OutputFormat.YYYYMMDD);

        BigDecimal cycDebit = acct.currCycDebit().signum() == 0
                ? DEFAULT_CYC_DEBIT
                : acct.currCycDebit();

        return new OutAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                padRight(convertedReissueDate, 10),
                acct.currCycCredit(),
                cycDebit,
                padRight(acct.groupId(), 10)
        );
    }

    /**
     * 1400-POPUL-ARRAY-RECORD — build the array-structured record.
     * Slots 1-3 are populated with specific values; slots 4-5 remain zero
     * (equivalent to COBOL INITIALIZE).
     */
    ArrayAccountRecord buildArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(ArrayAccountRecord.ENTRY_COUNT);
        entries.add(new BalanceEntry(acct.currBal(), ARR_DEBIT_1));
        entries.add(new BalanceEntry(acct.currBal(), ARR_DEBIT_2));
        entries.add(new BalanceEntry(ARR_BAL_3, ARR_DEBIT_3));
        entries.add(BalanceEntry.ZERO);
        entries.add(BalanceEntry.ZERO);
        return new ArrayAccountRecord(acct.acctId(), List.copyOf(entries));
    }

    /**
     * 1500-POPUL-VBRC-RECORD — build the short status record.
     */
    VarLengthRecord.StatusRecord buildStatusRecord(AccountRecord acct) {
        return new VarLengthRecord.StatusRecord(acct.acctId(), acct.activeStatus());
    }

    /**
     * 1500-POPUL-VBRC-RECORD — build the long balance record.
     * The reissue year is extracted from the YYYY-MM-DD reissue date.
     */
    VarLengthRecord.BalanceRecord buildBalanceRecord(AccountRecord acct) {
        String reissueYear = acct.reissueDate().substring(0, 4);
        return new VarLengthRecord.BalanceRecord(
                acct.acctId(), acct.currBal(), acct.creditLimit(), reissueYear);
    }

    // ---- formatters ----

    static String formatOutRecord(OutAccountRecord r) {
        return String.format("%011d", r.acctId())
                + r.activeStatus()
                + CobolDataParser.formatSignedDecimal(r.currBal(), 12, 2)
                + CobolDataParser.formatSignedDecimal(r.creditLimit(), 12, 2)
                + CobolDataParser.formatSignedDecimal(r.cashCreditLimit(), 12, 2)
                + padRight(r.openDate(), 10)
                + padRight(r.expirationDate(), 10)
                + padRight(r.reissueDate(), 10)
                + CobolDataParser.formatSignedDecimal(r.currCycCredit(), 12, 2)
                + CobolDataParser.formatSignedDecimal(r.currCycDebit(), 12, 2)
                + padRight(r.groupId(), 10);
    }

    static String formatArrayRecord(ArrayAccountRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", r.acctId()));
        for (BalanceEntry e : r.balanceEntries()) {
            sb.append(CobolDataParser.formatSignedDecimal(e.currBal(), 12, 2));
            sb.append(CobolDataParser.formatSignedDecimal(e.currCycDebit(), 12, 2));
        }
        sb.append("    "); // ARR-FILLER PIC X(04)
        return sb.toString();
    }

    static String formatStatusRecord(VarLengthRecord.StatusRecord r) {
        return String.format("%011d", r.acctId()) + r.activeStatus();
    }

    static String formatBalanceRecord(VarLengthRecord.BalanceRecord r) {
        return String.format("%011d", r.acctId())
                + CobolDataParser.formatSignedDecimal(r.currBal(), 12, 2)
                + CobolDataParser.formatSignedDecimal(r.creditLimit(), 12, 2)
                + r.reissueYear();
    }

    String formatAccountDisplay(AccountRecord a) {
        return "ACCT-ID                 :" + String.format("%011d", a.acctId()) + "\n"
             + "ACCT-ACTIVE-STATUS      :" + a.activeStatus() + "\n"
             + "ACCT-CURR-BAL           :" + CobolDataParser.formatSignedDecimal(a.currBal(), 12, 2) + "\n"
             + "ACCT-CREDIT-LIMIT       :" + CobolDataParser.formatSignedDecimal(a.creditLimit(), 12, 2) + "\n"
             + "ACCT-CASH-CREDIT-LIMIT  :" + CobolDataParser.formatSignedDecimal(a.cashCreditLimit(), 12, 2) + "\n"
             + "ACCT-OPEN-DATE          :" + a.openDate() + "\n"
             + "ACCT-EXPIRAION-DATE     :" + a.expirationDate() + "\n"
             + "ACCT-REISSUE-DATE       :" + a.reissueDate() + "\n"
             + "ACCT-CURR-CYC-CREDIT    :" + CobolDataParser.formatSignedDecimal(a.currCycCredit(), 12, 2) + "\n"
             + "ACCT-CURR-CYC-DEBIT     :" + CobolDataParser.formatSignedDecimal(a.currCycDebit(), 12, 2) + "\n"
             + "ACCT-GROUP-ID           :" + a.groupId() + "\n"
             + "-------------------------------------------------";
    }

    private void display(String msg) {
        displayLog.add(msg);
        System.out.println(msg);
    }

    public List<String> getDisplayLog() {
        return List.copyOf(displayLog);
    }

    private static String padRight(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }

    // ---- CLI entry point ----

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println(
                    "Usage: AccountBatchProcessor <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        var processor = new AccountBatchProcessor(
                Path.of(args[0]), Path.of(args[1]),
                Path.of(args[2]), Path.of(args[3]));

        int count = processor.execute();
        System.out.println("Processed " + count + " account records.");
    }
}
