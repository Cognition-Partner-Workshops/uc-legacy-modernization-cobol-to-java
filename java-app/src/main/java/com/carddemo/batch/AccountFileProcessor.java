package com.carddemo.batch;

import com.carddemo.model.AccountRecord;
import com.carddemo.model.ArrayAccountRecord;
import com.carddemo.model.OutAccountRecord;
import com.carddemo.model.VbrRecord1;
import com.carddemo.model.VbrRecord2;
import com.carddemo.util.DateConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Java 17+ rewrite of COBOL batch program CBACT01C.
 *
 * Reads every record from the indexed account master file (ACCTFILE)
 * and writes three derivative output files:
 *   OUTFILE   -- flat fixed-length records with selected/transformed fields
 *   ARRYFILE  -- array-structured records (5 balance/debit slots per account)
 *   VBRCFILE  -- variable-length records (two records per account)
 */
public class AccountFileProcessor {

    private static final BigDecimal DEFAULT_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal DEBIT_SLOT_1 = new BigDecimal("1005.00");
    private static final BigDecimal DEBIT_SLOT_2 = new BigDecimal("1525.00");
    private static final BigDecimal BALANCE_SLOT_3 = new BigDecimal("-1025.00");
    private static final BigDecimal DEBIT_SLOT_3 = new BigDecimal("-2500.00");

    private final Path acctFilePath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;
    private final PrintStream console;

    public AccountFileProcessor(Path acctFilePath, Path outFilePath,
                                 Path arryFilePath, Path vbrcFilePath) {
        this(acctFilePath, outFilePath, arryFilePath, vbrcFilePath, System.out);
    }

    public AccountFileProcessor(Path acctFilePath, Path outFilePath,
                                 Path arryFilePath, Path vbrcFilePath,
                                 PrintStream console) {
        this.acctFilePath = acctFilePath;
        this.outFilePath = outFilePath;
        this.arryFilePath = arryFilePath;
        this.vbrcFilePath = vbrcFilePath;
        this.console = console;
    }

    /**
     * Execute the batch job. Returns the number of records processed.
     */
    public int execute() throws IOException {
        console.println("START OF EXECUTION OF PROGRAM CBACT01C");

        int recordCount = 0;

        try (BufferedReader reader = openInputFile();
             BufferedWriter outWriter = openOutputFile(outFilePath);
             BufferedWriter arryWriter = openOutputFile(arryFilePath);
             BufferedWriter vbrcWriter = openOutputFile(vbrcFilePath)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                AccountRecord account = AccountRecord.parse(line);
                displayAccountRecord(account);

                OutAccountRecord outRecord = populateOutRecord(account);
                writeRecord(outWriter, outRecord.toOutputLine(), "OUTFILE");

                ArrayAccountRecord arrRecord = populateArrayRecord(account);
                writeRecord(arryWriter, arrRecord.toOutputLine(), "ARRYFILE");

                VbrRecord1 vbr1 = populateVbrRecord1(account);
                VbrRecord2 vbr2 = populateVbrRecord2(account);
                writeRecord(vbrcWriter, vbr1.toOutputLine(), "VBRCFILE");
                writeRecord(vbrcWriter, vbr2.toOutputLine(), "VBRCFILE");

                recordCount++;
            }
        }

        console.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return recordCount;
    }

    private BufferedReader openInputFile() throws IOException {
        if (!Files.exists(acctFilePath)) {
            console.println("ERROR OPENING ACCTFILE");
            throw new IOException("Account file not found: " + acctFilePath);
        }
        return Files.newBufferedReader(acctFilePath);
    }

    private BufferedWriter openOutputFile(Path path) throws IOException {
        try {
            return Files.newBufferedWriter(path);
        } catch (IOException e) {
            console.println("ERROR OPENING " + path.getFileName());
            throw e;
        }
    }

    private void writeRecord(BufferedWriter writer, String record,
                              String fileName) throws IOException {
        try {
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {
            console.println(fileName + " WRITE STATUS IS: ERROR");
            throw e;
        }
    }

    /**
     * Mirrors paragraph 1100-DISPLAY-ACCT-RECORD.
     */
    private void displayAccountRecord(AccountRecord acct) {
        console.println("ACCT-ID                 :" + acct.acctId());
        console.println("ACCT-ACTIVE-STATUS      :" + acct.activeStatus());
        console.println("ACCT-CURR-BAL           :" + acct.currentBalance());
        console.println("ACCT-CREDIT-LIMIT       :" + acct.creditLimit());
        console.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit());
        console.println("ACCT-OPEN-DATE          :" + acct.openDate());
        console.println("ACCT-EXPIRAION-DATE     :" + acct.expirationDate());
        console.println("ACCT-REISSUE-DATE       :" + acct.reissueDate());
        console.println("ACCT-CURR-CYC-CREDIT    :" + acct.currentCycleCredit());
        console.println("ACCT-CURR-CYC-DEBIT     :" + acct.currentCycleDebit());
        console.println("ACCT-GROUP-ID           :" + acct.groupId());
        console.println("-------------------------------------------------");
    }

    /**
     * Mirrors paragraph 1300-POPUL-ACCT-RECORD.
     * Applies date conversion and default-debit substitution.
     */
    OutAccountRecord populateOutRecord(AccountRecord acct) {
        String reissueDate = DateConverter.convert(
                acct.reissueDate(), DateConverter.YYYY_MM_DD, DateConverter.YYYY_MM_DD);

        BigDecimal cycleDebit = acct.currentCycleDebit();
        if (cycleDebit.compareTo(BigDecimal.ZERO) == 0) {
            cycleDebit = DEFAULT_DEBIT;
        }

        return new OutAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currentBalance(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reissueDate,
                acct.currentCycleCredit(),
                cycleDebit,
                acct.groupId()
        );
    }

    /**
     * Mirrors paragraph 1400-POPUL-ARRAY-RECORD.
     * Slots 1-3 populated with specific values; slots 4-5 remain zero.
     */
    ArrayAccountRecord populateArrayRecord(AccountRecord acct) {
        BigDecimal[] balances = new BigDecimal[ArrayAccountRecord.SLOT_COUNT];
        BigDecimal[] debits = new BigDecimal[ArrayAccountRecord.SLOT_COUNT];

        for (int i = 0; i < ArrayAccountRecord.SLOT_COUNT; i++) {
            balances[i] = BigDecimal.ZERO.setScale(2);
            debits[i] = BigDecimal.ZERO.setScale(2);
        }

        balances[0] = acct.currentBalance();
        debits[0] = DEBIT_SLOT_1;

        balances[1] = acct.currentBalance();
        debits[1] = DEBIT_SLOT_2;

        balances[2] = BALANCE_SLOT_3;
        debits[2] = DEBIT_SLOT_3;

        return new ArrayAccountRecord(acct.acctId(), balances, debits);
    }

    /**
     * Mirrors paragraph 1500-POPUL-VBRC-RECORD (first record).
     */
    VbrRecord1 populateVbrRecord1(AccountRecord acct) {
        return new VbrRecord1(acct.acctId(), acct.activeStatus());
    }

    /**
     * Mirrors paragraph 1500-POPUL-VBRC-RECORD (second record).
     * Extracts just the year portion from the reissue date.
     */
    VbrRecord2 populateVbrRecord2(AccountRecord acct) {
        String reissueYear = acct.reissueDate().length() >= 4
                ? acct.reissueDate().substring(0, 4) : acct.reissueDate();
        return new VbrRecord2(
                acct.acctId(),
                acct.currentBalance(),
                acct.creditLimit(),
                reissueYear
        );
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println(
                    "Usage: AccountFileProcessor <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of(args[0]), Path.of(args[1]),
                Path.of(args[2]), Path.of(args[3]));

        try {
            int count = processor.execute();
            System.out.println("Processed " + count + " account records.");
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }
}
