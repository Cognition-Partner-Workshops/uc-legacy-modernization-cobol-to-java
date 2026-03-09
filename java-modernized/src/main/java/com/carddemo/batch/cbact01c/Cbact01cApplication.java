package com.carddemo.batch.cbact01c;

import com.carddemo.batch.cbact01c.io.AccountFileReader;
import com.carddemo.batch.cbact01c.io.OutputWriter;
import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.model.ArrayAccountRecord;
import com.carddemo.batch.cbact01c.model.OutAccountRecord;
import com.carddemo.batch.cbact01c.model.VbrRecord1;
import com.carddemo.batch.cbact01c.model.VbrRecord2;
import com.carddemo.batch.cbact01c.service.AccountProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ modernization of the COBOL batch program CBACT01C.
 *
 * <p>The original COBOL program reads account master records sequentially
 * from a VSAM KSDS file (ACCTFILE) and writes transformed data into three
 * output files:
 * <ul>
 *   <li>OUTFILE  — selected account fields with date reformatting and
 *                   default-debit substitution</li>
 *   <li>ARRYFILE — array-based records with 5 balance/debit pairs</li>
 *   <li>VBRCFILE — variable-length records (two per account)</li>
 * </ul>
 *
 * <p>Usage: {@code java -jar cbact01c-java.jar <acctfile> <outfile> <arryfile> <vbrcfile>}
 */
public final class Cbact01cApplication {

    private Cbact01cApplication() {
        // entry-point class
    }

    /**
     * Processes the account file and writes the three output files.
     *
     * @param acctFilePath path to the input account data file
     * @param outFilePath  path for the output account file (CSV)
     * @param arryFilePath path for the array output file (CSV)
     * @param vbrcFilePath path for the VBR output file (CSV)
     * @return the number of account records processed
     * @throws IOException if any file I/O fails
     */
    public static int process(Path acctFilePath, Path outFilePath,
                              Path arryFilePath, Path vbrcFilePath)
            throws IOException {

        System.out.println("START OF EXECUTION OF PROGRAM CBACT01C");

        // 0000-ACCTFILE-OPEN / read all records
        List<AccountRecord> accounts = AccountFileReader.readAll(acctFilePath);

        // Accumulate output records
        List<OutAccountRecord> outRecords = new ArrayList<>();
        List<ArrayAccountRecord> arrRecords = new ArrayList<>();
        List<VbrRecord1> vb1Records = new ArrayList<>();
        List<VbrRecord2> vb2Records = new ArrayList<>();

        for (AccountRecord acct : accounts) {
            // 1100-DISPLAY-ACCT-RECORD
            displayAccountRecord(acct);

            // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
            outRecords.add(AccountProcessor.toOutRecord(acct));

            // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
            arrRecords.add(AccountProcessor.toArrayRecord(acct));

            // 1500-POPUL-VBRC-RECORD + 1550/1575-WRITE-VBx-RECORD
            vb1Records.add(AccountProcessor.toVbrRecord1(acct));
            vb2Records.add(AccountProcessor.toVbrRecord2(acct));
        }

        // Write output files
        OutputWriter.writeOutFile(outRecords, outFilePath);
        OutputWriter.writeArrayFile(arrRecords, arryFilePath);
        OutputWriter.writeVbrFile(vb1Records, vb2Records, vbrcFilePath);

        System.out.println("END OF EXECUTION OF PROGRAM CBACT01C");
        return accounts.size();
    }

    /**
     * Displays account record fields to stdout, replicating
     * COBOL paragraph 1100-DISPLAY-ACCT-RECORD.
     */
    private static void displayAccountRecord(AccountRecord acct) {
        System.out.println("ACCT-ID                 :" + String.format("%011d", acct.acctId()));
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

    /**
     * CLI entry point.
     *
     * @param args acctfile outfile arryfile vbrcfile
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println(
                    "Usage: cbact01c <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        try {
            int count = process(
                    Path.of(args[0]),
                    Path.of(args[1]),
                    Path.of(args[2]),
                    Path.of(args[3])
            );
            System.out.println("Processed " + count + " account records.");
        } catch (IOException e) {
            System.err.println("ABENDING PROGRAM");
            System.err.println("FILE STATUS IS: " + e.getMessage());
            System.exit(999);
        }
    }
}
