package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayRecord;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbRecord1;
import com.carddemo.batch.cbact01c.model.VbRecord2;
import com.carddemo.batch.cbact01c.util.DateFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ migration of COBOL batch program <strong>CBACT01C</strong>.
 *
 * <p>Reads an account master file (ACCTFILE) sequentially and writes three
 * output files:
 * <ol>
 *   <li><strong>OUTFILE</strong> — flat account records with date reformatting
 *       and debit defaulting.</li>
 *   <li><strong>ARRYFILE</strong> — array records (account ID + 5 balance/debit
 *       pairs).</li>
 *   <li><strong>VBRCFILE</strong> — variable-length records (two per account:
 *       a short status record and a longer financial-summary record).</li>
 * </ol>
 *
 * <p>This class is intentionally kept simple and testable: all I/O goes through
 * {@link java.nio.file.Path} parameters so tests can point at temp files.
 */
public class Cbact01cProcessor {

    private final Path acctFilePath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;
    private final PrintStream console;

    /** Counts exposed for testing / verification. */
    private int recordsRead;
    private int recordsWritten;

    public Cbact01cProcessor(Path acctFilePath, Path outFilePath,
                             Path arryFilePath, Path vbrcFilePath) {
        this(acctFilePath, outFilePath, arryFilePath, vbrcFilePath, System.out);
    }

    public Cbact01cProcessor(Path acctFilePath, Path outFilePath,
                             Path arryFilePath, Path vbrcFilePath,
                             PrintStream console) {
        this.acctFilePath = acctFilePath;
        this.outFilePath = outFilePath;
        this.arryFilePath = arryFilePath;
        this.vbrcFilePath = vbrcFilePath;
        this.console = console;
    }

    /**
     * Execute the batch job — mirrors the COBOL PROCEDURE DIVISION main flow.
     */
    public void execute() throws IOException {
        console.println("START OF EXECUTION OF PROGRAM CBACT01C");

        List<AccountRecord> accounts = readAccounts();
        try (
                BufferedWriter outWriter = Files.newBufferedWriter(outFilePath, StandardCharsets.UTF_8);
                BufferedWriter arryWriter = Files.newBufferedWriter(arryFilePath, StandardCharsets.UTF_8);
                BufferedWriter vbrcWriter = Files.newBufferedWriter(vbrcFilePath, StandardCharsets.UTF_8)
        ) {
            for (AccountRecord acct : accounts) {
                displayAccountRecord(acct);

                // 1300 + 1350: populate and write OUT record
                String formattedReissue = DateFormatter.stripDashes(acct.acctReissueDate());
                OutAccountRecord outRec = OutAccountRecord.fromAccount(acct, formattedReissue);
                outWriter.write(outRec.toDelimitedLine());
                outWriter.newLine();

                // 1400 + 1450: populate and write ARRAY record
                ArrayRecord arrRec = ArrayRecord.fromAccount(acct);
                arryWriter.write(arrRec.toDelimitedLine());
                arryWriter.newLine();

                // 1500 + 1550 + 1575: populate and write VB records
                VbRecord1 vb1 = VbRecord1.fromAccount(acct);
                vbrcWriter.write(vb1.toDelimitedLine());
                vbrcWriter.newLine();

                VbRecord2 vb2 = VbRecord2.fromAccount(acct);
                vbrcWriter.write(vb2.toDelimitedLine());
                vbrcWriter.newLine();

                recordsWritten++;
            }
        }

        console.println("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    /**
     * Read and parse all account records from the input file.
     * Mirrors COBOL paragraphs {@code 0000-ACCTFILE-OPEN} and
     * {@code 1000-ACCTFILE-GET-NEXT} + {@code 9000-ACCTFILE-CLOSE}.
     */
    List<AccountRecord> readAccounts() throws IOException {
        List<AccountRecord> accounts = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(acctFilePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                accounts.add(AccountRecord.parse(line));
                recordsRead++;
            }
        }
        return accounts;
    }

    /**
     * Display account fields to the console — mirrors {@code 1100-DISPLAY-ACCT-RECORD}.
     */
    private void displayAccountRecord(AccountRecord acct) {
        console.println("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
        console.println("ACCT-ACTIVE-STATUS      :" + acct.acctActiveStatus());
        console.println("ACCT-CURR-BAL           :" + acct.acctCurrBal().toPlainString());
        console.println("ACCT-CREDIT-LIMIT       :" + acct.acctCreditLimit().toPlainString());
        console.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.acctCashCreditLimit().toPlainString());
        console.println("ACCT-OPEN-DATE          :" + acct.acctOpenDate());
        console.println("ACCT-EXPIRAION-DATE     :" + acct.acctExpirationDate());
        console.println("ACCT-REISSUE-DATE       :" + acct.acctReissueDate());
        console.println("ACCT-CURR-CYC-CREDIT    :" + acct.acctCurrCycCredit().toPlainString());
        console.println("ACCT-CURR-CYC-DEBIT     :" + acct.acctCurrCycDebit().toPlainString());
        console.println("ACCT-GROUP-ID           :" + acct.acctGroupId());
        console.println("-------------------------------------------------");
    }

    public int getRecordsRead() {
        return recordsRead;
    }

    public int getRecordsWritten() {
        return recordsWritten;
    }

    // -----------------------------------------------------------------------
    // CLI entry point
    // -----------------------------------------------------------------------

    /**
     * Command-line entry point.
     *
     * <p>Usage: {@code java -jar cbact01c.jar <acctfile> <outfile> <arryfile> <vbrcfile>}
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01cProcessor <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Cbact01cProcessor processor = new Cbact01cProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3])
        );
        processor.execute();

        System.out.println("Records read   : " + processor.getRecordsRead());
        System.out.println("Records written: " + processor.getRecordsWritten());
    }
}
