package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.CobolDecimalParser;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord.BalanceEntry;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbRecord1;
import com.carddemo.batch.model.VbRecord2;
import com.carddemo.batch.util.DateConverter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java translation of COBOL program CBACT01C.CBL.
 *
 * Reads the VSAM account file sequentially and writes three output files:
 * <ol>
 *   <li><b>OUTFILE</b> — flat reformatted account records</li>
 *   <li><b>ARRYFILE</b> — array-structured records with 5 balance/debit entries</li>
 *   <li><b>VBRCFILE</b> — two variable-length records per account (short + long)</li>
 * </ol>
 */
public final class AccountFileProcessor {

    private static final BigDecimal DEFAULT_CYCLE_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arrayFile;
    private final Path vbrcFile;

    public AccountFileProcessor(Path inputFile, Path outFile, Path arrayFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arrayFile = arrayFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Runs the batch process, returning the list of parsed account records
     * for inspection by tests.
     */
    public List<AccountRecord> process() throws IOException {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        List<AccountRecord> accounts = AccountFileReader.readAll(inputFile);

        try (BufferedWriter outWriter = Files.newBufferedWriter(outFile);
             BufferedWriter arrWriter = Files.newBufferedWriter(arrayFile);
             BufferedWriter vbrWriter = Files.newBufferedWriter(vbrcFile)) {

            for (AccountRecord acct : accounts) {
                displayAccountRecord(acct);

                OutputAccountRecord outRec = buildOutputRecord(acct);
                writeOutputRecord(outWriter, outRec);

                ArrayAccountRecord arrRec = buildArrayRecord(acct);
                writeArrayRecord(arrWriter, arrRec);

                VbRecord1 vb1 = buildVbRecord1(acct);
                VbRecord2 vb2 = buildVbRecord2(acct);
                writeVbRecord1(vbrWriter, vb1);
                writeVbRecord2(vbrWriter, vb2);
            }
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return accounts;
    }

    // -- 1100-DISPLAY-ACCT-RECORD --

    private static void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + acct.acctId());
        System.out.println("ACCT-ACTIVE-STATUS      :" + acct.activeStatus());
        System.out.println("ACCT-CURR-BAL           :" + acct.currBal());
        System.out.println("ACCT-CREDIT-LIMIT       :" + acct.creditLimit());
        System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit());
        System.out.println("ACCT-OPEN-DATE          :" + acct.openDate());
        System.out.println("ACCT-EXPIRAION-DATE     :" + acct.expirationDate());
        System.out.println("ACCT-REISSUE-DATE       :" + acct.reissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    :" + acct.currCycCredit());
        System.out.println("ACCT-CURR-CYC-DEBIT     :" + acct.currCycDebit());
        System.out.println("ACCT-GROUP-ID           :" + acct.groupId());
        System.out.println("-------------------------------------------------");
    }

    // -- 1300-POPUL-ACCT-RECORD --

    static OutputAccountRecord buildOutputRecord(AccountRecord acct) {
        String convertedReissueDate = DateConverter.convertDashToCompact(acct.reissueDate());

        BigDecimal cycDebit = acct.currCycDebit().compareTo(BigDecimal.ZERO) == 0
                ? DEFAULT_CYCLE_DEBIT
                : acct.currCycDebit();

        return new OutputAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                convertedReissueDate,
                acct.currCycCredit(),
                cycDebit,
                acct.groupId()
        );
    }

    // -- 1400-POPUL-ARRAY-RECORD --

    static ArrayAccountRecord buildArrayRecord(AccountRecord acct) {
        List<BalanceEntry> entries = new ArrayList<>(ArrayAccountRecord.NUM_ENTRIES);
        entries.add(new BalanceEntry(acct.currBal(), ARRAY_DEBIT_1));
        entries.add(new BalanceEntry(acct.currBal(), ARRAY_DEBIT_2));
        entries.add(new BalanceEntry(ARRAY_BAL_3, ARRAY_DEBIT_3));
        entries.add(new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        entries.add(new BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));

        return new ArrayAccountRecord(acct.acctId(), entries, "    ");
    }

    // -- 1500-POPUL-VBRC-RECORD --

    static VbRecord1 buildVbRecord1(AccountRecord acct) {
        return new VbRecord1(acct.acctId(), acct.activeStatus());
    }

    static VbRecord2 buildVbRecord2(AccountRecord acct) {
        return new VbRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                DateConverter.extractYear(acct.reissueDate())
        );
    }

    // -- Write routines --

    /**
     * Writes the output account record as a fixed-width line.
     * Fields use zoned-decimal for display numerics and COMP-3 encoding
     * for cycle-debit (rendered as zoned-decimal in the ASCII text output).
     */
    private static void writeOutputRecord(BufferedWriter writer, OutputAccountRecord rec)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(rec.acctId());
        sb.append(rec.activeStatus());
        sb.append(CobolDecimalParser.formatZonedDecimal(rec.currBal(), 12, 2));
        sb.append(CobolDecimalParser.formatZonedDecimal(rec.creditLimit(), 12, 2));
        sb.append(CobolDecimalParser.formatZonedDecimal(rec.cashCreditLimit(), 12, 2));
        sb.append(padRight(rec.openDate(), 10));
        sb.append(padRight(rec.expirationDate(), 10));
        sb.append(padRight(rec.reissueDate(), 10));
        sb.append(CobolDecimalParser.formatZonedDecimal(rec.currCycCredit(), 12, 2));
        sb.append(CobolDecimalParser.formatZonedDecimal(rec.currCycDebit(), 12, 2));
        sb.append(padRight(rec.groupId(), 10));
        writer.write(sb.toString());
        writer.newLine();
    }

    /**
     * Writes the array record — account ID, 5 balance/debit pairs, 4-byte filler.
     */
    private static void writeArrayRecord(BufferedWriter writer, ArrayAccountRecord rec)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(rec.acctId());
        for (BalanceEntry entry : rec.balanceEntries()) {
            sb.append(CobolDecimalParser.formatZonedDecimal(entry.currBal(), 12, 2));
            sb.append(CobolDecimalParser.formatZonedDecimal(entry.currCycDebit(), 12, 2));
        }
        sb.append(padRight(rec.filler(), 4));
        writer.write(sb.toString());
        writer.newLine();
    }

    /**
     * Writes VB1 — short variable-length record (12 bytes).
     */
    private static void writeVbRecord1(BufferedWriter writer, VbRecord1 rec) throws IOException {
        writer.write(rec.acctId() + rec.activeStatus());
        writer.newLine();
    }

    /**
     * Writes VB2 — longer variable-length record (39 bytes).
     */
    private static void writeVbRecord2(BufferedWriter writer, VbRecord2 rec) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(rec.acctId());
        sb.append(CobolDecimalParser.formatZonedDecimal(rec.currBal(), 12, 2));
        sb.append(CobolDecimalParser.formatZonedDecimal(rec.creditLimit(), 12, 2));
        sb.append(padRight(rec.reissueYear(), 4));
        writer.write(sb.toString());
        writer.newLine();
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

    // -- Main entry point --

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println(
                    "Usage: AccountFileProcessor <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }
        var processor = new AccountFileProcessor(
                Path.of(args[0]), Path.of(args[1]), Path.of(args[2]), Path.of(args[3]));
        processor.process();
    }
}
