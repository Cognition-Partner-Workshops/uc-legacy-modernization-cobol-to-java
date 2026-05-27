package com.carddemo.batch;

import com.carddemo.batch.model.*;
import com.carddemo.batch.util.DateConverter;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ port of the COBOL batch program CBACT01C.cbl.
 * <p>
 * Reads an account file (indexed VSAM KSDS in the original, flat-file here),
 * transforms each record, and writes three output files:
 * <ol>
 *   <li><b>OUTFILE</b> — selected account fields with date reformatting</li>
 *   <li><b>ARRYFILE</b> — array-based balance records (5 entries per account)</li>
 *   <li><b>VBRCFILE</b> — two variable-length record types per account</li>
 * </ol>
 */
public class Cbact01cBatchJob {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1     = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2     = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3       = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3     = new BigDecimal("-2500.00");

    private final Path acctFilePath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;

    public Cbact01cBatchJob(Path acctFile, Path outFile, Path arryFile, Path vbrcFile) {
        this.acctFilePath = acctFile;
        this.outFilePath  = outFile;
        this.arryFilePath = arryFile;
        this.vbrcFilePath = vbrcFile;
    }

    /**
     * Executes the batch job — the equivalent of the COBOL PROCEDURE DIVISION.
     */
    public BatchResult execute() {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");
        int recordCount = 0;

        try (
            BufferedReader reader = Files.newBufferedReader(acctFilePath, StandardCharsets.UTF_8);
            BufferedWriter outWriter  = Files.newBufferedWriter(outFilePath,  StandardCharsets.UTF_8);
            BufferedWriter arryWriter = Files.newBufferedWriter(arryFilePath, StandardCharsets.UTF_8);
            BufferedWriter vbrcWriter = Files.newBufferedWriter(vbrcFilePath, StandardCharsets.UTF_8)
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                AccountRecord acct = AccountRecord.parse(line);
                displayAccountRecord(acct);

                OutputAccountRecord outRec = populateOutputRecord(acct);
                writeLine(outWriter, outRec.toDelimited());

                ArrayRecord arrRec = populateArrayRecord(acct);
                writeLine(arryWriter, arrRec.toDelimited());

                VbRecord1 vb1 = populateVbRecord1(acct);
                VbRecord2 vb2 = populateVbRecord2(acct);
                writeLine(vbrcWriter, vb1.toDelimited());
                writeLine(vbrcWriter, vb2.toDelimited());

                recordCount++;
            }
        } catch (IOException e) {
            System.err.println("ERROR READING/WRITING FILE: " + e.getMessage());
            System.err.println("ABENDING PROGRAM");
            return new BatchResult(recordCount, BatchResult.RC_ABEND,
                    "I/O error: " + e.getMessage());
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return new BatchResult(recordCount, BatchResult.RC_OK, "Completed successfully");
    }

    // ── record transformation methods (mirrors COBOL paragraphs) ──

    /**
     * 1300-POPUL-ACCT-RECORD: builds the output account record.
     * Calls the date converter (replacing COBDATFT) and applies
     * the default cycle-debit rule.
     */
    OutputAccountRecord populateOutputRecord(AccountRecord acct) {
        String reformattedReissueDate = DateConverter.convert(
                acct.reissueDate(), "2", "2");

        BigDecimal cycDebit = acct.currCycDebit().signum() == 0
                ? DEFAULT_CYC_DEBIT
                : acct.currCycDebit();

        return new OutputAccountRecord(
                acct.acctId(),
                acct.activeStatus(),
                acct.currBal(),
                acct.creditLimit(),
                acct.cashCreditLimit(),
                acct.openDate(),
                acct.expirationDate(),
                reformattedReissueDate,
                acct.currCycCredit(),
                cycDebit,
                acct.groupId()
        );
    }

    /**
     * 1400-POPUL-ARRAY-RECORD: builds the array balance record.
     * Elements 1-3 are populated with a mix of actual and hard-coded values;
     * elements 4-5 are zero-initialised (mirrors the COBOL INITIALIZE).
     */
    ArrayRecord populateArrayRecord(AccountRecord acct) {
        List<ArrayRecord.BalanceEntry> entries = new ArrayList<>(ArrayRecord.ENTRY_COUNT);
        entries.add(new ArrayRecord.BalanceEntry(acct.currBal(),  ARRAY_DEBIT_1));
        entries.add(new ArrayRecord.BalanceEntry(acct.currBal(),  ARRAY_DEBIT_2));
        entries.add(new ArrayRecord.BalanceEntry(ARRAY_BAL_3,     ARRAY_DEBIT_3));
        entries.add(new ArrayRecord.BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        entries.add(new ArrayRecord.BalanceEntry(BigDecimal.ZERO, BigDecimal.ZERO));
        return new ArrayRecord(acct.acctId(), List.copyOf(entries));
    }

    /**
     * 1500-POPUL-VBRC-RECORD (VB1): short record with ID + status.
     */
    VbRecord1 populateVbRecord1(AccountRecord acct) {
        return new VbRecord1(acct.acctId(), acct.activeStatus());
    }

    /**
     * 1500-POPUL-VBRC-RECORD (VB2): longer record with balances and reissue year.
     */
    VbRecord2 populateVbRecord2(AccountRecord acct) {
        String reissueYear = acct.reissueDate().length() >= 4
                ? acct.reissueDate().substring(0, 4)
                : acct.reissueDate();
        return new VbRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                reissueYear
        );
    }

    // ── I/O helpers ──

    private static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    private static void displayAccountRecord(AccountRecord acct) {
        System.out.printf("ACCT-ID                 :%011d%n", acct.acctId());
        System.out.printf("ACCT-ACTIVE-STATUS      :%s%n",    acct.activeStatus());
        System.out.printf("ACCT-CURR-BAL           :%s%n",    acct.currBal().toPlainString());
        System.out.printf("ACCT-CREDIT-LIMIT       :%s%n",    acct.creditLimit().toPlainString());
        System.out.printf("ACCT-CASH-CREDIT-LIMIT  :%s%n",    acct.cashCreditLimit().toPlainString());
        System.out.printf("ACCT-OPEN-DATE          :%s%n",    acct.openDate());
        System.out.printf("ACCT-EXPIRAION-DATE     :%s%n",    acct.expirationDate());
        System.out.printf("ACCT-REISSUE-DATE       :%s%n",    acct.reissueDate());
        System.out.printf("ACCT-CURR-CYC-CREDIT    :%s%n",    acct.currCycCredit().toPlainString());
        System.out.printf("ACCT-CURR-CYC-DEBIT     :%s%n",    acct.currCycDebit().toPlainString());
        System.out.printf("ACCT-GROUP-ID           :%s%n",    acct.groupId());
        System.out.println("-------------------------------------------------");
    }

    // ── CLI entry point ──

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01cBatchJob <ACCTFILE> <OUTFILE> <ARRYFILE> <VBRCFILE>");
            System.exit(1);
        }

        Cbact01cBatchJob job = new Cbact01cBatchJob(
                Path.of(args[0]), Path.of(args[1]),
                Path.of(args[2]), Path.of(args[3]));

        BatchResult result = job.execute();
        System.out.printf("Records processed: %d | Return code: %d | %s%n",
                result.recordsProcessed(), result.returnCode(), result.message());

        if (!result.isSuccess()) {
            System.exit(result.returnCode());
        }
    }
}
