package com.carddemo.batch;

import com.carddemo.batch.io.FixedWidthWriter;
import com.carddemo.batch.io.PackedDecimalUtil;
import com.carddemo.batch.io.ZonedDecimalUtil;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.util.DateConverter;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java 17+ rewrite of CBACT01C.cbl.
 *
 * Reads an account master file (ACCTFILE, 300-byte fixed-width records)
 * and produces three output files:
 *
 *   OUTFILE   – flat account records (107 bytes, mixed DISPLAY/COMP-3)
 *   ARRYFILE  – array records with 5 balance/debit slots (110 bytes)
 *   VBRCFILE  – variable-length records (12-byte VB1 + 39-byte VB2 per account)
 *
 * Console output matches the original COBOL DISPLAY statements exactly.
 */
public class Cbact01c {

    private static final BigDecimal DEFAULT_CYC_DEBIT = new BigDecimal("2525.00");
    private static final BigDecimal ARR_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARR_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARR_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARR_DEBIT_3 = new BigDecimal("-2500.00");

    /**
     * Result holder for testability — captures all outputs.
     */
    public record BatchResult(
            List<String> consoleLines,
            byte[] outFileBytes,
            byte[] arryFileBytes,
            byte[] vbrcFileBytes
    ) {}

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: Cbact01c <acctfile> <outfile> <arryfile> <vbrcfile>");
            System.exit(1);
        }
        Path acctFile = Path.of(args[0]);
        Path outFile  = Path.of(args[1]);
        Path arryFile = Path.of(args[2]);
        Path vbrcFile = Path.of(args[3]);

        List<String> console = new ArrayList<>();
        console.add("START OF EXECUTION OF PROGRAM CBACT01C");

        try (OutputStream outOs   = Files.newOutputStream(outFile);
             OutputStream arryOs  = Files.newOutputStream(arryFile);
             OutputStream vbrcOs  = Files.newOutputStream(vbrcFile)) {

            processAccounts(acctFile, outOs, arryOs, vbrcOs, console);
        }

        console.add("END OF EXECUTION OF PROGRAM CBACT01C");
        console.forEach(System.out::println);
    }

    /**
     * Core processing — separated from main() for unit-test access.
     */
    public static BatchResult run(Path acctFile) throws IOException {
        List<String> console = new ArrayList<>();
        console.add("START OF EXECUTION OF PROGRAM CBACT01C");

        ByteArrayOutputStream outBaos  = new ByteArrayOutputStream();
        ByteArrayOutputStream arryBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream vbrcBaos = new ByteArrayOutputStream();

        processAccounts(acctFile, outBaos, arryBaos, vbrcBaos, console);

        console.add("END OF EXECUTION OF PROGRAM CBACT01C");
        return new BatchResult(console, outBaos.toByteArray(),
                arryBaos.toByteArray(), vbrcBaos.toByteArray());
    }

    /**
     * Overload that accepts raw input text for testing without files.
     */
    public static BatchResult run(String acctFileContent) throws IOException {
        Path tmp = Files.createTempFile("acctfile", ".txt");
        try {
            Files.writeString(tmp, acctFileContent, StandardCharsets.ISO_8859_1);
            return run(tmp);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private static void processAccounts(Path acctFile,
                                        OutputStream outOs,
                                        OutputStream arryOs,
                                        OutputStream vbrcOs,
                                        List<String> console) throws IOException {

        FixedWidthWriter outWriter  = new FixedWidthWriter(outOs);
        FixedWidthWriter arryWriter = new FixedWidthWriter(arryOs);

        try (BufferedReader reader = Files.newBufferedReader(acctFile, StandardCharsets.ISO_8859_1)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                AccountRecord acct = AccountRecord.parse(line);

                // 1100-DISPLAY-ACCT-RECORD — emit to console
                console.add(acct.toRawLine());
                for (String dl : acct.displayLines().split("\n")) {
                    console.add(dl);
                }

                // 1300-POPUL-ACCT-RECORD + date conversion
                String convertedReissueDate = DateConverter.convert(
                        acct.acctReissueDate(), "2", "2");

                BigDecimal outCycDebit = acct.acctCurrCycDebit().signum() == 0
                        ? DEFAULT_CYC_DEBIT
                        : acct.acctCurrCycDebit();

                // 1350-WRITE-ACCT-RECORD
                outWriter.writeOutRecord(
                        acct.acctId(), acct.acctActiveStatus(),
                        acct.acctCurrBal(), acct.acctCreditLimit(),
                        acct.acctCashCreditLimit(),
                        acct.acctOpenDate(), acct.acctExpiraionDate(),
                        convertedReissueDate,
                        acct.acctCurrCycCredit(), outCycDebit,
                        acct.acctGroupId());

                // 1400-POPUL-ARRAY-RECORD
                BigDecimal[] balances = new BigDecimal[5];
                BigDecimal[] debits   = new BigDecimal[5];
                balances[0] = acct.acctCurrBal();
                debits[0]   = ARR_DEBIT_1;
                balances[1] = acct.acctCurrBal();
                debits[1]   = ARR_DEBIT_2;
                balances[2] = ARR_BAL_3;
                debits[2]   = ARR_DEBIT_3;
                balances[3] = BigDecimal.ZERO;
                debits[3]   = BigDecimal.ZERO;
                balances[4] = BigDecimal.ZERO;
                debits[4]   = BigDecimal.ZERO;

                // 1450-WRITE-ARRY-RECORD
                arryWriter.writeArrayRecord(acct.acctId(), balances, debits);

                // 1500-POPUL-VBRC-RECORD
                String reissueYyyy = acct.acctReissueDate().substring(0, 4);

                // Build VB1: 12 bytes (acctId 11 + activeStatus 1)
                String vb1Str = padRight(acct.acctId(), 11)
                        + padRight(acct.acctActiveStatus(), 1);
                byte[] vb1 = vb1Str.getBytes(StandardCharsets.ISO_8859_1);

                // Build VB2: 39 bytes (acctId 11 + currBal 12 + creditLimit 12 + reissueYyyy 4)
                ByteArrayOutputStream vb2Baos = new ByteArrayOutputStream(39);
                vb2Baos.write(padRight(acct.acctId(), 11)
                        .getBytes(StandardCharsets.ISO_8859_1));
                vb2Baos.write(ZonedDecimalUtil.format(acct.acctCurrBal(), 12, 2)
                        .getBytes(StandardCharsets.ISO_8859_1));
                vb2Baos.write(ZonedDecimalUtil.format(acct.acctCreditLimit(), 12, 2)
                        .getBytes(StandardCharsets.ISO_8859_1));
                vb2Baos.write(padRight(reissueYyyy, 4)
                        .getBytes(StandardCharsets.ISO_8859_1));
                byte[] vb2 = vb2Baos.toByteArray();

                // DISPLAY VBRC records
                console.add("VBRC-REC1:" + vb1Str);
                console.add("VBRC-REC2:" + acct.acctId()
                        + ZonedDecimalUtil.format(acct.acctCurrBal(), 12, 2)
                        + ZonedDecimalUtil.format(acct.acctCreditLimit(), 12, 2)
                        + reissueYyyy);

                // 1550-WRITE-VB1-RECORD + 1575-WRITE-VB2-RECORD
                writeVariableRecord(vbrcOs, vb1);
                writeVariableRecord(vbrcOs, vb2);
            }
        }

        outWriter.close();
        arryWriter.close();
    }

    private static void writeVariableRecord(OutputStream os, byte[] data) throws IOException {
        os.write(data);
        os.write('\n');
    }

    private static String padRight(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s.substring(0, len);
        return s + " ".repeat(len - s.length());
    }
}
