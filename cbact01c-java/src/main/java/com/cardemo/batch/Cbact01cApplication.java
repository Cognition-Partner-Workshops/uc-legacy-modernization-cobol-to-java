package com.cardemo.batch;

import com.cardemo.batch.model.*;
import com.cardemo.batch.service.AccountFileProcessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Java 17+ equivalent of CBACT01C.cbl batch program.
 *
 * Reads an indexed account file (VSAM KSDS → flat file in Java) sequentially,
 * transforms each record, and writes three output files:
 *   1. OUTFILE  - flat account output (one record per account)
 *   2. ARRYFILE - array record output (balance array per account)
 *   3. VBRCFILE - variable-length record output (two records per account)
 *
 * Usage: java -jar cbact01c-batch.jar <acctfile> <outfile> <arryfile> <vbrcfile>
 */
public class Cbact01cApplication {

    private static final Logger LOG = Logger.getLogger(Cbact01cApplication.class.getName());

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01cApplication <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }

        Path acctFilePath = Path.of(args[0]);
        Path outFilePath = Path.of(args[1]);
        Path arryFilePath = Path.of(args[2]);
        Path vbrcFilePath = Path.of(args[3]);

        try {
            int processed = processAccountFile(acctFilePath, outFilePath, arryFilePath, vbrcFilePath);
            LOG.info("END OF EXECUTION OF PROGRAM CBACT01C. Records processed: " + processed);
        } catch (IOException e) {
            LOG.severe("ABENDING PROGRAM: " + e.getMessage());
            System.exit(999);
        }
    }

    /**
     * Main processing loop — mirrors the PROCEDURE DIVISION logic.
     *
     * @return the number of records successfully processed
     */
    public static int processAccountFile(Path acctFile, Path outFile, Path arryFile, Path vbrcFile)
            throws IOException {

        LOG.info("START OF EXECUTION OF PROGRAM CBACT01C");

        if (!Files.exists(acctFile)) {
            throw new IOException("ERROR OPENING ACCTFILE: file not found - " + acctFile);
        }

        int recordCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(acctFile);
             BufferedWriter outWriter = Files.newBufferedWriter(outFile);
             BufferedWriter arryWriter = Files.newBufferedWriter(arryFile);
             BufferedWriter vbrcWriter = Files.newBufferedWriter(vbrcFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                AccountRecord acct = AccountRecord.parse(line);

                // 1100-DISPLAY-ACCT-RECORD equivalent
                logAccountRecord(acct);

                // 1300-POPUL-ACCT-RECORD + 1350-WRITE-ACCT-RECORD
                OutputAccountRecord outRec = AccountFileProcessor.buildOutputRecord(acct);
                outWriter.write(outRec.toDelimitedString());
                outWriter.newLine();

                // 1400-POPUL-ARRAY-RECORD + 1450-WRITE-ARRY-RECORD
                ArrayRecord arrRec = AccountFileProcessor.buildArrayRecord(acct);
                arryWriter.write(arrRec.toDelimitedString());
                arryWriter.newLine();

                // 1500-POPUL-VBRC-RECORD + 1550/1575-WRITE-VB*-RECORD
                VbRecord1 vb1 = AccountFileProcessor.buildVbRecord1(acct);
                vbrcWriter.write(vb1.toDelimitedString());
                vbrcWriter.newLine();

                VbRecord2 vb2 = AccountFileProcessor.buildVbRecord2(acct);
                vbrcWriter.write(vb2.toDelimitedString());
                vbrcWriter.newLine();

                recordCount++;
            }
        }

        return recordCount;
    }

    private static void logAccountRecord(AccountRecord acct) {
        LOG.fine(() -> String.format("""
                ACCT-ID                 :%d
                ACCT-ACTIVE-STATUS      :%s
                ACCT-CURR-BAL           :%s
                ACCT-CREDIT-LIMIT       :%s
                ACCT-CASH-CREDIT-LIMIT  :%s
                ACCT-OPEN-DATE          :%s
                ACCT-EXPIRAION-DATE     :%s
                ACCT-REISSUE-DATE       :%s
                ACCT-CURR-CYC-CREDIT    :%s
                ACCT-CURR-CYC-DEBIT     :%s
                ACCT-GROUP-ID           :%s
                -------------------------------------------------""",
                acct.acctId(), acct.activeStatus(), acct.currBal(),
                acct.creditLimit(), acct.cashCreditLimit(),
                acct.openDate(), acct.expirationDate(), acct.reissueDate(),
                acct.currCycCredit(), acct.currCycDebit(), acct.groupId()));
    }
}
