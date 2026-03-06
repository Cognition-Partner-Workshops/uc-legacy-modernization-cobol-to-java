package com.carddemo.batch;

import com.carddemo.batch.io.AccountFileReader;
import com.carddemo.batch.io.BatchFileWriter;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayAccountRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import com.carddemo.batch.model.VbrcRecord1;
import com.carddemo.batch.model.VbrcRecord2;
import com.carddemo.batch.service.AccountProcessor;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Java 17+ modernization of the COBOL batch program CBACT01C.
 * <p>
 * Original program purpose: reads an indexed VSAM account file (ACCTFILE)
 * sequentially and writes each record into three output files in different
 * formats:
 * <ul>
 *   <li><b>OUTFILE</b> — flat sequential file with selected account fields,
 *       date conversion, and zero-debit substitution</li>
 *   <li><b>ARRYFILE</b> — array-based records with 5 balance/debit entries</li>
 *   <li><b>VBRCFILE</b> — variable-length records (two per account: VB1 and VB2)</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 *   java -jar cbact01c-modernized.jar &lt;acctfile&gt; &lt;outfile&gt; &lt;arryfile&gt; &lt;vbrcfile&gt;
 * </pre>
 */
public final class Cbact01cApplication {

    private Cbact01cApplication() {
        // entry-point class
    }

    /**
     * Main entry point — mirrors the COBOL PROCEDURE DIVISION.
     *
     * @param args four file paths: acctfile, outfile, arryfile, vbrcfile
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: cbact01c <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Path acctFilePath = Path.of(args[0]);
        Path outFilePath = Path.of(args[1]);
        Path arryFilePath = Path.of(args[2]);
        Path vbrcFilePath = Path.of(args[3]);

        long processed = process(acctFilePath, outFilePath, arryFilePath, vbrcFilePath);
        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C — " + processed + " records processed.");
    }

    /**
     * Core processing method that can be called programmatically (e.g., from tests).
     * <p>
     * Opens the input account file and the three output files, reads each
     * account record, transforms it, and writes the results. This mirrors the
     * COBOL PROCEDURE DIVISION flow:
     * <ol>
     *   <li>0000-ACCTFILE-OPEN / 2000-OUTFILE-OPEN / 3000-ARRFILE-OPEN / 4000-VBRFILE-OPEN</li>
     *   <li>PERFORM UNTIL END-OF-FILE loop with 1000-ACCTFILE-GET-NEXT</li>
     *   <li>9000-ACCTFILE-CLOSE</li>
     * </ol>
     *
     * @param acctFile the input account file
     * @param outFile  the flat output file
     * @param arryFile the array output file
     * @param vbrcFile the variable-length record output file
     * @return the number of records processed
     */
    public static long process(Path acctFile, Path outFile, Path arryFile, Path vbrcFile) {
        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        AtomicLong count = new AtomicLong(0);

        try (AccountFileReader reader = new AccountFileReader(acctFile);
             BatchFileWriter outWriter = new BatchFileWriter(outFile, "OUTFILE");
             BatchFileWriter arryWriter = new BatchFileWriter(arryFile, "ARRYFILE");
             BatchFileWriter vbrcWriter = new BatchFileWriter(vbrcFile, "VBRCFILE")) {

            reader.stream().forEach(account -> {
                // 1100-DISPLAY-ACCT-RECORD
                displayAccountRecord(account);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutputAccountRecord outRec = AccountProcessor.buildOutputRecord(account);
                outWriter.writeLine(outRec.toOutputLine());

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayAccountRecord arrRec = AccountProcessor.buildArrayRecord(account);
                arryWriter.writeLine(arrRec.toOutputLine());

                // 1500-POPUL-VBRC-RECORD + 1550-WRITE-VB1-RECORD
                VbrcRecord1 vb1 = AccountProcessor.buildVbrcRecord1(account);
                vbrcWriter.writeLine(vb1.toOutputLine());

                // 1575-WRITE-VB2-RECORD
                VbrcRecord2 vb2 = AccountProcessor.buildVbrcRecord2(account);
                vbrcWriter.writeLine(vb2.toOutputLine());

                count.incrementAndGet();
            });
        }

        return count.get();
    }

    /**
     * Displays the account record fields to stdout, mirroring
     * paragraph 1100-DISPLAY-ACCT-RECORD.
     */
    private static void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + acct.formattedAcctId());
        System.out.println("ACCT-ACTIVE-STATUS      :" + acct.activeStatus());
        System.out.println("ACCT-CURR-BAL           :" + acct.currentBalance().toPlainString());
        System.out.println("ACCT-CREDIT-LIMIT       :" + acct.creditLimit().toPlainString());
        System.out.println("ACCT-CASH-CREDIT-LIMIT  :" + acct.cashCreditLimit().toPlainString());
        System.out.println("ACCT-OPEN-DATE          :" + acct.openDate());
        System.out.println("ACCT-EXPIRAION-DATE     :" + acct.expirationDate());
        System.out.println("ACCT-REISSUE-DATE       :" + acct.reissueDate());
        System.out.println("ACCT-CURR-CYC-CREDIT    :" + acct.currentCycleCredit().toPlainString());
        System.out.println("ACCT-CURR-CYC-DEBIT     :" + acct.currentCycleDebit().toPlainString());
        System.out.println("ACCT-GROUP-ID           :" + acct.groupId());
        System.out.println("-------------------------------------------------");
    }
}
